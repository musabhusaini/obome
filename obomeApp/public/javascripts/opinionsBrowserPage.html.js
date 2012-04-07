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
						
						$(docTextContainer).html(result.document.text);
						
						$(summaryTable).append("<tr><th>Aspect</th><th>Polarity</th></tr>");
						
						var data = [];
						var max = 0;
						
						$.each(result.aspectOpinionMap, function(key, value) {
							$(summaryTable)
								.append("<tr><td>" + key + "</td><td>" + value + "</td></tr>");
							
							// Can't put very long labels.
							var maxLength = 12;
							if (key.length > maxLength) {
								var end = key.search(/\s+/);
								end = (end >= 0 && end <= maxLength) ? end : maxLength-3;
								key = key.substring(0, end) + "...";
							}
							data.push([key, value]);
							
							var absVal = window.Math.abs(value);
							if (max < absVal) {
								max = absVal;
							}
						});
						
						if (!data.length) {
							$(summaryTable)
								.empty()
								.text("Not enough data available to generate a summary");
						}
						
						var graphCanvasId = $(graphCanvas)
							.empty()
							.attr("id");
						
						var plot = data.length && $.jqplot(graphCanvasId, [ data ], {
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
					});
			})
			.find("option:first")
				.attr("selected", true);
		
		$(reviewsList).change();
	};
})(window, window.document, window.jQuery, window.obome, window.obome.routes)