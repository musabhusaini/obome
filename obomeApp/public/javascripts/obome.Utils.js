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
	
})(window, window.document, window.jQuery, window.obome);