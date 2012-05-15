(function(window, document, $, Math) {
	
	$.widget("widgets.spinner", {
		options: {
			spinnerClass: "ui-icon-loader",
			positionMy: "center center",
			positionAt: "center center"
		},
		
		_spinner: null,
		
		_theRest: null,
		
		refresh: function() {
			// Calculate new dimensions.
			var dims = {
				width: $(this.element).width() < $(this._spinner).width() ? $(this._spinner).width() : "",
				height: $(this.element).height() < $(this._spinner).height() ? $(this._spinner).height() : ""
			};
			
			// Remove everything else.
			$(this._theRest).remove();
			
			// Resize if necessary and append the spinner.
			$(this.element)
				.width(dims.width)
				.height(dims.height)
				.append(this._spinner);

			// Add classes and move to the center.
			$(this._spinner)
				.removeClass()
				.addClass(this.options.spinnerClass)
				.addClass("ui-widget")
				.addClass("ui-corner-all")
				.position({
					my: this.options.positionMy,
					at: this.options.positionAt,
					of: $(this.element),
					collision: "none"
				});
		},
	
		_create: function() {
			// Save everything else.
			this._theRest = $(this.element).contents();
			
			// Create spinner.
			this._spinner = "spinner_" + window.Math.floor(window.Math.random() * 1000000).toString();
			this._spinner = $("<div>").attr("id", this._spinner);
		},
		
		_init: function() {
			this.refresh();
		},
		
		_setOption: function(key, value) {
			$.Widget.prototype._setOption.apply(this, arguments);
			
			if ("key" === "spinnerClass") {
				this.refresh();
			}
		},
		
		destroy: function() {
			$(this._spinner).remove();
			this._spinner = null;

			$(this.element)
				.append(this._theRest)
				.width("")
				.height("");

			$.Widget.prototype.destroy.call(this);
		}
	});

})(window, window.document, window.jQuery, window.Math);