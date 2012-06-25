(function(window, document, $, Math, obome, routes, Utils) {

	obome.displayPage = function(options) {
		
		var docContainer = "#documentContainer";
		var docTextContainer = "#documentTextContainer";
		var reviewsList = "#lstReviews";
		var summaryContainer = "#cntnrOpinionSummary";
		var summaryTable = "#tblOpinionSummary";
		var graphContainer = "#cntnrOpinionGraph";
		var graphCanvas = "#cnvsOpinionGraph";
		
		var opinionDependentClass = ".opinion-dependent";
		var summaryBoxClass = ".ob-summary-box";
				
		options = $.extend(true, {
			scorePrecision: 3,
			prefetch: {
				enabled: true,
				interval: 1000,
				count: 50
			}
		}, options);
		
		var lastResult = {};
				
		function getPolarityIndicator(polarity) {
			return "[" + (polarity < 0 ? "-" : "+") + "]";
		}
		
		function roundScore(score) {
			return score.toFixed(options.scorePrecision);
		}
		
		function updateSummary() {
			var max = 0;
			var count = 0;
			var graphData = [];
			
			$.each(lastResult.scorecard, function(key, value) {
				count++;
			});

			$(summaryTable)
				.empty()
				.append((count && "<tr><th>Aspect</th><th>Polarity</th></tr>") || "Not enough data available to generate a summary");
			
			var overallScore;
			
			$.each(lastResult.scorecard, function(key, value) {
				value = roundScore(value);
				
				if (key.toLowerCase() === "overall") {
					overallScore = value;
					return true;
				}
				
				$(summaryTable)
					.append("<tr><td>" + key + "</td><td>" + value + "</td></tr>");
				
				// Can't put very long labels.
				var maxLength = 40/count;
				if (key.length > maxLength) {
					var end = key.search(/\s+/);
					end = (end >= 0 && end <= maxLength) ? end : maxLength-3;
					key = key.substring(0, end) + "...";
				}
				graphData.push([key, value]);
				
				var absVal = window.Math.abs(value);
				if (max < absVal) {
					max = absVal;
				}
			});
			
			if (overallScore) {
				$(summaryTable)
					.append("<tr class='ob-divider'><td></td><td></td></tr>")
					.append("<tr class='ob-overall-rating'><td>Overall</td><td>" + overallScore + "</td></tr>");
			}
			
			var graphCanvasId = $(graphCanvas)
				.empty()
				.attr("id");
			
			max = roundScore(max * 1.1);
			graphData.length && $.jqplot(graphCanvasId, [ graphData ], {
				axesDefaults: {
				},
				seriesDefaults: {
					renderer: $.jqplot.BarRenderer,
					rendererOptions: { fillToZero: true }
				},
				axes: {
					xaxis: {
				        renderer: $.jqplot.CategoryAxisRenderer,
				        tickRenderer: $.jqplot.CanvasAxisTickRenderer,
				        tickOptions: {
				        	fontFamily: "Courier New",
				        	fontSize: "9pt"
					    }
					},
					yaxis: {
						min: -max,
						max: max,
						tickOptions: {
						}
					}
				}
			});
		}

		function fetchOM(uuid) {
			return $.getJSON(routes.OpinionCollections.opinionMiner({
				collection: options.collection.uuid,
				document: uuid
			}));
		}
		
		function updateListItemUI(result, ui) {
			var polarity = result.scorecard["Overall"];
			var text = $(ui).text();
			if (!text.match(/\[[\+\-]\]$/)) {
				$(ui).text(text + " " + getPolarityIndicator(polarity));
			}
		}
		
		// Prefetching logic.
		var prefetcherDefaultState = {
			index: 0,
			thread: null,
			ajax: null
		};
		
		var prefetcherState = $.extend({ }, prefetcherDefaultState);
				
		function resumePrefetching() {
			if (options.prefetch.enabled && !window.isNaN(window.parseFloat(options.prefetch.interval)) &&
					window.isFinite(options.prefetch.interval)) {
				
				prefetcherState.thread = window.setTimeout(function() {
					var index = ++prefetcherState.index;
					
					var ui = $(reviewsList).find("option").eq(index);
					if (!$(ui).size() || ((options.prefetch.count >= 0) && (index >= options.prefetch.count))) {
						options.prefetch.enabled = false;
						prefetcherState = prefetcherDefaultState;
						return;
					}
					
					var uuid = $(ui).val();
					prefetcherState.ajax = fetchOM(uuid)
						.success(function(result) {
							updateListItemUI(result, ui);
							resumePrefetching();
						});
				}, (options.prefetch.interval) || 1000);
			}
		}
		
		function pausePrefetching() {
			if (prefetcherState) {
				if (prefetcherState.thread) {
					window.clearTimeout(prefetcherState.thread);
					prefetcherState.thread = null;
				}
				
				if (prefetcherState.ajax) {
					prefetcherState.ajax.abort();
					prefetcherState.ajax = null;
					prefetcherState.index--;
				}
			}
		}
		
		$(window).resize(updateSummary);
		
		$(reviewsList)
			.change(function(event) {
				var ui = $(event.target).find(":selected:first");
				var uuid = $(ui).val();
				if (!uuid) {
					return false;
				}
				
				$(docTextContainer).empty();
				$(summaryTable).empty();
				$(graphCanvas).empty();
				
				$(opinionDependentClass).spinner();
				
				// Stop prefetching because we want to prioritize this.
				pausePrefetching();
				
				fetchOM(uuid)
					.success(function(result) {
						$(opinionDependentClass).spinner("destroy");
						
						var doc = new obome.parsedDocument(result.document.text);
						var span = doc.getJQHtml();
						
						$(span).find(".doc-keyword")
							.addClass("ob-keyword-display")
							.each(function(index, element) {
								$(element).attr("title", "aspect: " + $(element).data("aspect"));
							});
						
						$(span).find(".doc-modifier.doc-polar, .doc-sentence-polarity")
							.each(function(index, element) {
								$(element).attr("title", "polarity: " + roundScore($(element).data("polarity") || 0));
							});
						
						$(span).find(".doc-sentence-polarity")
							.addClass("ob-sentence-polarity-display")
							.each(function(index, element) {
								$(element).text(" " + getPolarityIndicator($(element).data("polarity")));
							});

						$(span).find(".doc-modifier.doc-polar")
							.addClass("ob-modifier-display");

						$(span).find(".doc-irrelevant")
							.addClass("ob-irrelevant-display");
						
						$(docTextContainer).append(span);
						
						lastResult = result;
						updateSummary();
						updateListItemUI(result, ui);
						
						// Resume prefetching after we're done.
						resumePrefetching();
					});
			})
			.find("option:first")
				.attr("selected", true);
		
		$(reviewsList).change();
	};
})(window, window.document, window.jQuery, window.Math, window.obome, window.obome.routes, window.obome.Utils);