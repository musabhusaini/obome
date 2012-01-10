(function(window, document, $, UrlStore, Util) {

	var headerLabel = "dw_header";
	var textContainer = "dw_textContainer";
	var countSpan = "dw_count";
	var controlsContainer = "dw_controlsContainer";
	var nextButton = "dw_nextButton";
	var prevButton = "dw_prevButton";
	var bypassCacheToggle = "dw_bypassCacheToggle";
	
	var navButtonsClass = "navButton";

	// Helpers to make things easier.
	var makeId = function(domIdPrefix, id) {
		return domIdPrefix + "_" + id;
	}
	var $$ = function(domIdPrefix, id) {
		return "#" + makeId(domIdPrefix, id);
	};
	
	$.widget("widgets.documentDisplay", {
		options: {
			header: "Review",
			uuids: null,
			offset: 0,
			bypassCache: false
		},
		
		_id: null,
		
		_container: null,
		
		refresh: function() {
			var me = this;
			var id = me._id;
			var uuids = me.options.uuids;
			var index = me.options.offset;
			
			if (index < 0 || index >= uuids.length) {
				return;
			}

			//Util.displaySpinner($(me._container).find($$(textContainer, id)));
			$(me._container).find($$(textContainer, id)).spinner();
			$.getJSON(UrlStore.getDocument(uuids[index], me.options.bypassCache), function(doc) {
				var pattern = /\\feature\{(.+?)\}/mg;
				var text = doc.text;
				var match = null;
				text = text.replace(pattern, "<span class='ol-feature-element'>$1</span>");
				
				$(me._container).find($$(textContainer, id)).spinner("destroy");
				$(me._container)
					.find($$(textContainer, id))
					.empty()
					.html(window.unescape(text));
				
				$(me._container)
					.find($$(countSpan, id))
					.text((index+1).toString() + " of " + uuids.length.toString());
				
				$(me._container).find(".ol-feature-element")
					.button();
			});

			$(me._container).find($$(prevButton, id)).button("option", {
				disabled: (index === 0)
			});

			$(me._container).find($$(nextButton, id)).button("option", {
				disabled: (index === uuids.length-1)
			});
		},
	
		_create: function() {
			var me = this;
			var id = me._id = window.Math.floor(window.Math.random() * 1000000).toString();
			
			var goForward = function() {
				me.option({ offset: me.options.offset+1 });
			};
			
			var goBack = function() {
				me.option({ offset: me.options.offset-1 });
			};
			
			// Define the container that will keep everything else.
			me._container = $("<ul>")
				.addClass("ui-widget")
				.addClass("ui-controls-list")
				.appendTo(me.element);
			
			// Define the header.
			var headerElem = $("<div>")
				.addClass("ui-widget-header")
				.addClass("ui-corner-top")
				.addClass("ol-header")
				.attr("id", makeId(headerLabel, id))
				.text(me.options.header)
				.appendTo($("<li>")
						.appendTo(me._container));

			$("<span>")
				.attr("id", makeId(countSpan, id))
				.appendTo(headerElem)
				.css("float", "right");

			// Define the text container.
			var textContainerElem = $("<div>")
				.addClass("ui-widget-content")
				.attr("id", makeId(textContainer, id))
				.addClass("ol-document-text")
				.appendTo($("<li>")
						.appendTo(me._container));
			
			// Define the controls container.
			var controlsContainerElem = $("<ul>")
				.addClass("ui-sidebyside-controls-list")
				.attr("id", makeId(controlsContainer, id))
				.appendTo($("<li>")
						.addClass("ui-controls-list-item-spaced")
						.appendTo(me._container));

			// Define the previous button.
			var prevButtonElem = $("<li>")
				.attr("id", makeId(prevButton, id))
				.addClass(navButtonsClass)
				.button({
					disabled: true,
					label: "Previous",
					icons: {
						primary: "ui-icon-circle-triangle-w"
					}
				})
				.click(goBack)
				.appendTo(controlsContainerElem);
			
			// Define the next button.
			var nextButtonElem = $("<li>")
				.attr("id", makeId(nextButton, id))
				.addClass(navButtonsClass)
				.addClass("ui-sidebyside-controls-list-item-spaced")
				.button({
					disabled: true,
					label: "Next",
					icons: {
						secondary: "ui-icon-circle-triangle-e"
					}
				})
				.click(goForward)
				.appendTo(controlsContainerElem);
			
			// Define the bypass cache checkbox area.
			$("<li>")
				.addClass("ui-sidebyside-controls-list-item-spaced")
				.append($("<input type='checkbox' id='" + makeId(bypassCacheToggle, id) + "'/>")
					.attr("checked", me.options.bypassCache)
					.click(function() {
						me.option({ bypassCache: !me.options.bypassCache });
					}))
				.append("<label for='" + makeId(bypassCacheToggle, id) + "'>Bypass Cache</label>")
				.appendTo(controlsContainerElem);
			
			$(me._container).hide();
		},
		
		_init: function() {
			var me = this;
			var id = me._id;
			
			$(me._container).show()
			
			if (!me.options.uuids) {
				//Util.displaySpinner($(me._container).find($$(textContainer, id)));
				$(me._container).find($$(textContainer, id)).spinner();
				$.getJSON(UrlStore.getDocumentList(), function(uuids) {
					$(me._container).find($$(textContainer, id)).spinner("destroy");
					me.options.offset = 0;
					me.option({
						uuids: uuids
					});
				});
			} else {
				me.refresh();
			}
		},
		
		_setOption: function(key, value) {
			$.Widget.prototype._setOption.apply(this, arguments);
			
			if (key === "header") {
				$(this._container).find($$(headerLabel, this._id)).text(value);
			} else if (key === "uuids" || key === "offset") {
				this.refresh();
			} else if (key === "bypassCache") {
				// Nothing to do for now.
			}
		},
		
		destroy: function() {
			$(this._container).remove();
			this._container = null;
			
			$.Widget.prototype.destroy.call(this);
		}
	});
	
})(window, window.document, window.jQuery, window.ontologyLearner.UrlStore, window.ontologyLearner.Util);