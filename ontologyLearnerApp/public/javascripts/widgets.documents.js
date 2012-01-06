(function(window, document, $, UrlStore, Util) {

	var headerLabel = "dw_header";
	var textContainer = "dw_textContainer";
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
			header: "Review:",
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
			$.getJSON(UrlStore.GetDocument(uuids[index], me.options.bypassCache), function(doc) {
				$(me._container).find($$(textContainer, id)).spinner("destroy");
				$(me._container)
					.find($$(textContainer, id))
					.empty()
					.html(window.unescape(doc.text));
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
			
			me._container = $(document.createElement("div"))
				.addClass("ui-widget")

			$(me.element)
				.append(me._container);

			// Define the header.
			var headerElem = $(document.createElement("div"))
				.addClass("ui-widget-header")
				.addClass("ui-corner-top")
				.attr("id", makeId(headerLabel, id))
				.text(me.options.header);
			
			// Define the text container.
			var textContainerElem = $(document.createElement("div"))
				.addClass("ui-widget-content")
				.attr("id", makeId(textContainer, id))
				.addClass("ui-document-text");

			// Define the controls container.
			var controlsContainerElem = $(document.createElement("div"))
				.attr("id", makeId(controlsContainer, id));

			// Define the previous button.
			var prevButtonElem = $(document.createElement("div"))
				.attr("id", makeId(prevButton, id))
				.addClass(navButtonsClass)
				.button({
					disabled: true,
					label: "Previous",
					icons: {
						primary: "ui-icon-circle-triangle-w"
					}
				})
				.click(goBack);
			
			// Define the next button.
			var nextButtonElem = $(document.createElement("div"))
				.attr("id", makeId(nextButton, id))
				.addClass(navButtonsClass)
				.button({
					disabled: true,
					label: "Next",
					icons: {
						secondary: "ui-icon-circle-triangle-e"
					}
				})
				.click(goForward);
			
			// Define the bypass cache checkbox area.
			var bypassCacheToggleElem = $("<input type='checkbox' id='" + makeId(bypassCacheToggle, id) + "'/>")
				.attr("checked", me.options.bypassCache);
			var bypassCacheContainer = $(document.createElement("div"))
				.append(bypassCacheToggleElem);
			$(bypassCacheToggleElem).after("<label for='" + makeId(bypassCacheToggle, id) + "'>Bypass Cache</label>");
			$(bypassCacheToggleElem)
				.click(function() {
					me.option({ bypassCache: !me.options.bypassCache });
				});

			// Attach all the controls to the control area.
			$(controlsContainerElem)
				.append(prevButtonElem)
				.append(nextButtonElem)
				.append(bypassCacheContainer);
			
			// Attach all controls to the main container.
			$(me._container)
				.append(headerElem)
				.append(textContainerElem)
				.append(controlsContainerElem);
			
			// Set positions of various controls.
			$(controlsContainerElem).position({
				my: "top",
				at: "bottom",
				of: $(textContainerElem),
				offset: "0 10",
				collision: "none"
			});
			
			$(nextButtonElem).position({
				my: "left",
				at: "right",
				of: $(prevButtonElem),
				offset: "10 0",
				collision: "none"
			});
			
			$(bypassCacheContainer).position({
				my: "left",
				at: "right",
				of: $(nextButtonElem),
				offset: "10 0",
				collision: "none"
			});
			
			$(me._container).hide();
		},
		
		_init: function() {
			var me = this;
			var id = me._id;
			
			$(me._container).show()
			
			if (!me.options.uuids) {
				//Util.displaySpinner($(me._container).find($$(textContainer, id)));
				$(me._container).find($$(textContainer, id)).spinner();
				$.getJSON(UrlStore.GetDocumentList(), function(uuids) {
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