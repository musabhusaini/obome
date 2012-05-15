(function(window, document, $, obome) {
	
	obome.widgets = obome.widgets || { };
	
	obome.widgets.infoPopup = function() {
		if (!(this instanceof obome.widgets.infoPopup)) {
			return new obome.widgets.infoPopup();
		}
		
		var me = this;
		var theRest;
		var popup;
		var oldTitle;
		
		// this makes sure we have the right value of "me".
		function proxy(fn) {
			return function() {
				me = this;
				var args = [ ];
				for (var index=0; index<arguments.length; index++) {
					args.push(arguments[index]);
				}
				
				fn.apply(me, args);
			}
		}
		
		function getTitle() {
			return me.options.title || $(me.element).attr("title");
		}
		
		me.options = {
			title: null,
			buttonOptions: {
				text: false,
				icons: {
					primary: "ui-icon-info"
				}
			},
			dialogOptions: {
				autoOpen: false,
				draggable: false,
				resizable: false,
				show: "clip",
				hide: "clip",
				position: ["right", "bottom"]
			},
			buttonClass: "ui-icon-button"
		};
		
		me._create = proxy(function() {
			// get everything else.
			theRest = $(me.element).children();
			
			// only use the first element.
			popup = $(theRest).eq(0);
			
			oldTitle = $(me.element).attr("title");
			
			// create the dialog.
			$(popup).dialog($.extend(true, { }, me.options.dialogOptions, {
				title: getTitle()
			}));
		});
		
		me._init = proxy(function() {
			// turn this into a button.
			$(me.element)
				.attr("title", getTitle())
				.addClass(me.options.buttonClass)
				.button(me.options.buttonOptions)
				.hover(function() {
					// show the dialog on hover in.
					$(popup).dialog("open");
					$(popup).parent().find(".ui-dialog-titlebar-close").hide();
				}, function() {
					// close the dialog on hover out.
					$(popup).dialog("close");
				});
		});
		
		me.destroy = proxy(function() {
			if (oldTitle) {
				$(me.element).attr("title", oldTitle);
			}
			
			$(me.element)
				.removeClass(me.options.buttonClass)
				.button("destroy");
			$(popup).dialog("destroy");
		});
	};
	
	obome.widgets.infoPopup.prototype = {
		options: {},
		destroy: function() {},
		_create: function() {},
		_init: function() {},
		_setOption: function(key, value) {}
	};
	
	$.widget("widgets.infoPopup", new obome.widgets.infoPopup());
	
})(window, window.document, window.jQuery, window.obome);