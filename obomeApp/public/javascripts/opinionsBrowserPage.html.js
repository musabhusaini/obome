(function(window, document, $, ontologyLearner, routes) {

	ontologyLearner.displayPage = function(options) {
		
		var docContainer = "#documentContainer";
		var reviewsList = "#lstReviews";
		var summaryContainer = "#cntnrOpinionSummary";
		var graphContainer = "#cntnrOpinionGraph";
		
		$(docContainer)
			.documentBrowser({
				header: "Review Text",
				featureType: "none",
				showNav: false,
				showCounter: false
			});
		
		$(reviewsList)
			.change(function(event) {
				var uuid = $(event.target).find(":selected:first").val();
				$(docContainer).documentBrowser("option", { uuid: uuid });
				
				$(summaryContainer)
					.empty()
					.spinner();
				$(graphContainer)
					.empty()
					.spinner();
				
				$.getJSON(routes.OpinionCollections.opinionMiner({ collection: options.collection.uuid, document: uuid }))
					.success(function(map) {
						$(summaryContainer).spinner("destroy");
						$(graphContainer).spinner("destroy");
						
						$.each(map, function(key, value) {
							$(summaryContainer)
								.append("<div>" + key + ": " + value + "</div>");
						});
					});
			})
			.find("option:first")
				.attr("selected", true);
		
		$(reviewsList).change();
	};
})(window, window.document, window.jQuery, window.ontologyLearner, window.ontologyLearner.routes)