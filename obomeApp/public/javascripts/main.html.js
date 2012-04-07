(function(window, document, $, obome, routes) {

	obome.displayMainPage = function(options) {
		function pingDelayed() {
			window.setTimeout(ping, options.pingInterval);
		}
		
		function ping() {
			$.post(routes.Application.Session.ping())
				.success(pingDelayed);
		}
		
		pingDelayed();
	};
})(window, window.document, window.jQuery, window.obome, window.obome.routes)