(function(window, document, $, ontologyLearner, routes) {

	ontologyLearner.displayPage = function(options) {
		
		var docContainer = "#documentContainer";
		var reviewsList = "#lstReviews";
		var summaryContainer = "#cntnrOpinionSummary";
		var summaryTable = "#tblOpinionSummary";
		var graphContainer = "#cntnrOpinionGraph";
		var graphCanvas = "#cnvsOpinionGraph";
		
		$(docContainer)
			.documentBrowser({
				header: "Review Text",
				featureType: "none",
				showNav: false,
				showCounter: false,
				documentContainerClass: "ob-document-text"
			});
		
		$(reviewsList)
			.change(function(event) {
				var uuid = $(event.target).find(":selected:first").val();
				if (!uuid) {
					return false;
				}
				
				$(docContainer).documentBrowser("option", { uuid: uuid });
				
				$(summaryTable).empty();
				$(summaryContainer).spinner();
				$(graphCanvas).empty();
				$(graphContainer).spinner();
				
				$.getJSON(routes.OpinionCollections.opinionMiner({ collection: options.collection.uuid, document: uuid }))
					.success(function(map) {
						$(summaryContainer).spinner("destroy");
						$(graphContainer).spinner("destroy");
						
						$(summaryTable)
							.append("<tr><th>Aspect</th><th>Polarity</th></tr>");
						
						var data = [];
						
						$.each(map, function(key, value) {
							$(summaryTable)
								.append("<tr><td>" + key + "</td><td>" + value + "</td></tr>");
							
							data.push([key, value]);
						});
						
						if (!data.length) {
							$(summaryTable).empty();
						}
						
						var graphCanvasId = $(graphCanvas)
							.empty()
							.attr("id");
						
						var plot = data.length && $.jqplot(graphCanvasId, [ data ], {
							axesDefaults: {
//								labelRenderer: $.jqplot.CanvasAxisLabelRenderer
							},
							seriesDefaults: {
								renderer: $.jqplot.BarRenderer,
								rendererOptions: { fillToZero: true }
							},
							axes: {
								xaxis: {
							        renderer: $.jqplot.CategoryAxisRenderer,
//							        label: "Aspect",
							        tickRenderer: $.jqplot.CanvasAxisTickRenderer,
							        tickOptions: {
							        	angle: -30,
							        	fontFamily: "Courier New",
							        	fontSize: "9pt"
								    }
								},
								yaxis: {
//									label: "Polarity",
								}
							}
						});
					});
			})
			.find("option:first")
				.attr("selected", true);
		
		$(reviewsList).change();
	};
})(window, window.document, window.jQuery, window.ontologyLearner, window.ontologyLearner.routes)