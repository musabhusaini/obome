(function(window, document, $, ontologyLearner, routes) {

	ontologyLearner.displayMainPage = function(options) {
		function pingDelayed() {
			window.setTimeout(ping, options.pingInterval);
		}
		
		function ping() {
			$.post(routes.Application.Session.ping())
				.success(pingDelayed);
		}
		
		pingDelayed();
	};
})(window, window.document, window.jQuery, window.ontologyLearner, window.ontologyLearner.routes)