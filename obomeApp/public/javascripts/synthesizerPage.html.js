(function(window, document, $, obome, routes) {

	obome.displayPage = function(options) {
		
		var propertiesContainer = "#cntnrProperties";
		var nameTextbox = "#txtName";
		var progressContainer = "#cntnrProgress";
		var progressBar = "#pbarProgress";
		var distillerContainer = "#cntnrDistiller";
		var distillerStatsGraph = "#grphDistillerStats"
		var thresholdTextbox = "#txtThreshold";
		var messageContainer = "#cntnrMessage";
		var nextButton = "#btnNext";
		
		var corpus = options.corpus;
		var collection;
		
		function renameCorpus(name) {
			$.post(routes.OpinionCollections.rename({ corpus: corpus.uuid }), { name: name })
				.success(function(success) {
					if (success) {
						corpus.name = name;
					} else {
						$(nameTextbox).val(corpus.name);
					}
					
					$(nameTextbox).effect("highlight", {}, 500);
				});
		}
		
		$(nameTextbox)
			.val(corpus.name)
			.focusout(function(event) {
				renameCorpus($(event.target).val());
			})
			.keyup(function(event) {
				if (event.keyCode === $.ui.keyCode.ESCAPE) {
					$(event.target).val(corpus.name);
				}
			})
			.keypress(function(event) {
				if (event.which === $.ui.keyCode.ENTER) {
					$(event.target).blur();
				}
			});

		$(nextButton)
			.button({
				disabled: true
			})
			.click(function() {
				$(nextButton).button("disable");
				$(nextButton).spinner();
				
				$.post(routes.OpinionCollections.distill({ collection: collection.uuid }), { threshold: $(thresholdTextbox).val() })
					.success(function(collection) {
						window.location.href = routes.OpinionCollections.aspectsBrowserPage({ collection: collection.uuid });
					});
			});
		
		$(progressBar)
			.progressbar({
				value: 0
			});
		
		$(thresholdTextbox)
			.keypress(function(event) {
				return !window.isNaN(String.fromCharCode(event.which));
			});
		
		$(distillerContainer)
			.hide();
		
		function updateProgress(options) {
			options = $.extend((options || {}), {
				url: routes.OpinionCollections.synthesizerProgress({ corpus: corpus.uuid }),
				startValue: 0.0,
				ratio: 1.0
			});
			
			// Get progress and update.
			$.getJSON(options.url)
				.success(function(progress) {
					var displayableProgress = options.startValue + (progress || 0.0) * options.ratio;
					$(progressBar).progressbar("option", { value: window.Math.round(displayableProgress * 100) });
					
					if (progress < 1.0) {
						window.setTimeout(function() {
							updateProgress(options);
						}, 100);
					}
				});
		}
		
		// Make the call.
		$.post(routes.OpinionCollections.synthesizer({ corpus: corpus.uuid }))
			.success(function(coll) {
				collection = coll;
				
				// Get the threshold map.
				$.get(routes.OpinionCollections.distill({ collection: collection.uuid }))
					.success(function(data) {

						$(progressBar).progressbar("option", { value: 100 });
						$(progressContainer).hide();//"highlight", {}, 1000);

						$(distillerContainer)
							.show();
						
						var distillerStatsGraphId = $(distillerStatsGraph)
							.empty()
							.attr("id");
						
						var plot = $.jqplot(distillerStatsGraphId, [ data ], {
//							title: "Select your threshold",
							
							axesDefaults: {
								labelRenderer: $.jqplot.CanvasAxisLabelRenderer
							},
							
							seriesDefaults: {
								rendererOptions: {
									smooth: true
								}
							},
							
							axes: {
								xaxis: {
									label: "Threshold (%)",
									pad: 0,
									tickOptions: {
										formatString: "%2.0f"
									}
								},
								yaxis: {
									label: "Corpus Reduction (%)",
									tickOptions: {
										formatString: "%2.0f"
									}
								}
							},
							
							highlighter: {
								show: true,
								sizeAdjust: 7.5
							},
							cursor: {
								show: false
							}
						});
						
						$(thresholdTextbox).val("0");
						
						$(distillerStatsGraph).bind("jqplotDataClick", function(event, seriesIndex, pointIndex, data) {
							$(thresholdTextbox).val(Math.round(data[0]));
						});
						
						$(nextButton).button("enable");
					});
				
				updateProgress({
					url: routes.OpinionCollections.distillerProgress({ collection: collection.uuid }),
					startValue: 0.75,
					ratio: 0.25
				});
			});
		
		updateProgress({
			ratio: 0.75
		});
	};
})(window, window.document, window.jQuery, window.obome, window.obome.routes)