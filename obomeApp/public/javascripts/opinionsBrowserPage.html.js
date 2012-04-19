(function(window, document, $, obome, routes) {

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
		
		var lastResult = {};
		
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
			
			$.each(lastResult.scorecard, function(key, value) {
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
			
			var graphCanvasId = $(graphCanvas)
				.empty()
				.attr("id");
			
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
						min: -max * 1.1,
						max: max * 1.1,
						tickOptions: {
						}
					}
				}
			});
		}
		
//		$(summaryBoxClass).resizable({
//		});
		
		$(window).resize(updateSummary);
		
		$(reviewsList)
			.change(function(event) {
				var uuid = $(event.target).find(":selected:first").val();
				if (!uuid) {
					return false;
				}
				
				$(docTextContainer).empty();
				$(summaryTable).empty();
				$(graphCanvas).empty();
				
				$(opinionDependentClass).spinner();
				
				$.getJSON(routes.OpinionCollections.opinionMiner({ collection: options.collection.uuid, document: uuid }))
					.success(function(result) {
						
						$(opinionDependentClass).spinner("destroy");
						
						var text = result.document.text;
						text = text.replace(/\\modifier\{(.+?)\}/g, "<span class='ob-modifier'>$1</span>");
						text = text.replace(/\\modified\{(.+?)\}/g, "<span class='ob-modified'>$1</span>");
						$(docTextContainer).html(text);
						
						lastResult = result;
						updateSummary();
					});
			})
			.find("option:first")
				.attr("selected", true);
		
		$(reviewsList).change();
	};
})(window, window.document, window.jQuery, window.obome, window.obome.routes)