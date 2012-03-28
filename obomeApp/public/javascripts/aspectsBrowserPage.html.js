(function(window, document, $, ontologyLearner, routes) {

	ontologyLearner.displayPage = function(options) {
		var docContainer = "#documentContainer";
		var aspectsContainer = "#aspectsContainer";
		var downloadButton = "#btnDownload";
		var nextButton = "#btnNext";
		
		$(docContainer)
			.documentBrowser($.extend(options, {
				header: "Review Text",
				featureClick: function(event, data) {
					$(aspectsContainer).aspectsBrowser("addKeyword", data);
				}
			}));

		$(aspectsContainer)
			.aspectsBrowser(options);
		
		$(downloadButton)
			.button()
			.click(function() {
				window.open(routes.Aspects.downloadableTextFile({ collection: options.collection.uuid }));
			});
		
		$(nextButton)
			.button()
			.click(function() {
				$(nextButton).spinner();
				window.location.href = routes.OpinionCollections.opinionsBrowserPage({ collection: options.collection.uuid });
			});
	};
})(window, window.document, window.jQuery, window.ontologyLearner, window.ontologyLearner.routes)