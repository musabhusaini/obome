(function(window, document, $, OntologyLearner, UrlStore) {
	OntologyLearner.Util = OntologyLearner.Util || {};

	OntologyLearner.Util.displaySpinner = function(element) {
		var spinner = document.createElement("img");
		$(spinner).attr("src", UrlStore.GetSpinner());
		$(element)
			.empty()
			.append(spinner);
	};
	
})(window, window.document, window.jQuery, window.OntologyLearner, window.OntologyLearner.UrlStore);