(function(window, document, $, OntologyLearner, UrlStore) {
	OntologyLearner.Util = OntologyLearner.Util || {};

	OntologyLearner.Util.displaySpinner = function(element) {
		$(element)
			.empty()
			.append("<img src='" + UrlStore.GetSpinner() + "' />");
	};
	
})(window, window.document, window.jQuery, window.OntologyLearner, window.OntologyLearner.UrlStore);