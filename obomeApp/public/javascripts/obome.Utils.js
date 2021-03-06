(function(window, document, $, obome) {
	obome.Utils = obome.Utils || {};

	obome.Utils.displayMessage = function(options) {
		var container = $("<div>")
			.attr("title", options.title || "Message")
			.html(options.message)
			.appendTo($(document))
			.dialog({
				autoOpen: true,
				modal: true,
				resizable: false,
				buttons: {
					OK: function() {
						$(this).dialog("close");
						$(container).remove();
					}
				}
			});
	};
	
	obome.Utils.outerHtml = function(html) {
		return $("<div>")
			.append(html)
			.html();
	};
	
})(window, window.document, window.jQuery, window.obome);