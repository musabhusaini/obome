(function(window, document, $, ontologyLearner, UrlStore) {
	ontologyLearner.Util = ontologyLearner.Util || {};

	ontologyLearner.Util.displaySpinner = function(element) {
		$(element)
			.empty()
			.append("<img src='" + UrlStore.GetSpinner() + "' />");
	};
	
})(window, window.document, window.jQuery, window.ontologyLearner, window.ontologyLearner.UrlStore);