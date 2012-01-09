(function(window, document, $) {
	
	$.widget("widgets.spinner", {
		options: {
			spinnerClass: "ui-spinner",
			positionMy: "center center",
			positionAt: "center center"
		},
		
		_spinner: null,
		
		_theRest: null,
		
		refresh: function() {
			$(this._spinner)
				.removeClass()
				.addClass(this.options.spinnerClass)
				.addClass("ui-widget");
		},
	
		_create: function() {
			// Save everything else.
			this._theRest = $(this.element).contents();

			// Create spinner.
			this._spinner = "spinner_" + window.Math.floor(window.Math.random() * 1000000).toString();
			this._spinner = $("<div>")
				.attr("id", this._spinner)
				.addClass("ui-spinner")
				.addClass("ui-widget")
				.addClass("ui-corner-all")
				.appendTo(this.element);
			
			// Remove everything else.
			$(this._theRest).remove();
			
			// Move to the center.
			$(this._spinner).position({
				my: this.options.positionMy,
				at: this.options.positionAt,
				of: $(this.element),
				collision: "none"
			});
			
			// Revert so we let _init do this work.
			$(this.element).append(this._theRest)
			$(this._spinner).hide();
		},
		
		_init: function() {
			$(this._theRest).remove();
			$(this._spinner).show();
		},
		
		_setOption: function(key, value) {
			$.Widget.prototype._setOption.apply(this, arguments);
			
			if ("key" === "spinnerClass") {
				refresh();
			}
		},
		
		destroy: function() {
			$(this._spinner).remove();
			$(this.element).append(this._theRest)
			this._spinner = null;
			
			$.Widget.prototype.destroy.call(this);
		}
	});

})(window, window.document, window.jQuery);