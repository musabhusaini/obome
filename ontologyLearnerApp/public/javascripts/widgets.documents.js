(function(window, document, $, UrlStore, Util) {

	var headerLabel = "dw_header";
	var textContainer = "dw_textContainer";
	var countContainer = "dw_countContainer";
	var countSpan = "dw_countSpan";
	var controlsContainer = "dw_controlsContainer";
	var nextButton = "dw_nextButton";
	var prevButton = "dw_prevButton";
	var bypassCacheToggle = "dw_bypassCacheToggle";
	var smartNounsToggle = "dw_smartNounsToggle";
	
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
			bypassCache: false,
			featureType: "nouns"
		},
		
		_id: null,
		
		_container: null,
		
		_refreshCountDisplay: function() {
			var me = this;
			$(me._container)
				.find($$(countContainer, me._id))
				.empty()
				.append($("<span>")
					.attr("id", makeId(countSpan, me._id))
					.text(me.options.offset+1)
					.hover(function(event) {
						$(event.target).addClass("ui-state-highlight", "fast");
					}, function(event) {
						$(event.target).removeClass("ui-state-highlight", "fast");
					})
					.click(function(event) {
						$(event.target)
							.empty()
							.append($("<input type='text'>")
								.addClass("ol-counter-textbox")
								.focusout(function() {
									me._refreshCountDisplay();
								})
								.keyup(function(event) {
									if (event.keyCode === $.ui.keyCode.ESCAPE) {
										me._refreshCountDisplay();
									}
								})
								.keypress(function(event) {
									if (event.which === $.ui.keyCode.ENTER) {
										var index = $(event.target).val();
										if (index >= 1 && index <= me.options.uuids.length) {
											me.option({ offset: index-1 });
										}
									} else if (window.isNaN(String.fromCharCode(event.which))) {
										return false;
									}
								}));
						$(".ol-counter-textbox").focus();
					}))
				.append(" of " + me.options.uuids.length.toString());
		},
		
		refresh: function() {
			var me = this;
			var id = me._id;
			var uuids = me.options.uuids;
			var index = me.options.offset;
			
			if (index < 0 || index >= uuids.length) {
				return;
			}

			$(me._container).find($$(textContainer, id)).spinner();
			$.getJSON(UrlStore.getDocument(uuids[index], me.options.featureType, me.options.bypassCache), function(doc) {
				var pattern = /\\feature\{(.+?)\}/mg;
				var text = doc.text;
				var match = null;
				text = text.replace(pattern, "<span class='ol-feature-element'>$1</span>");
				
				$(me._container).find($$(textContainer, id)).spinner("destroy");
				$(me._container)
					.find($$(textContainer, id))
					.empty()
					.html(window.unescape(text));
				
				me._refreshCountDisplay();
				
				$(me._container).find(".ol-feature-element")
					.button()
					.draggable({
						helper: "clone",
						revert: "invalid",
						revertDuration: 400,
						scope: "features",
						zIndex: 1000
					})
					.click(function(event) {
						me._trigger("featureClick", event, $(event.target).text());
					});
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
				.appendTo(me.element)
				// The header
				.append($("<li>")
					.append($("<div>")
						.addClass("ui-widget-header")
						.addClass("ui-corner-top")
						.addClass("ol-header")
						.attr("id", makeId(headerLabel, id))
						.text(me.options.header)
						.append($("<div>")
							.attr("id", makeId(countContainer, id))
							.css("float", "right"))))
				// The text container.
				.append($("<li>")
					.append($("<div>")
						.addClass("ui-widget-content")
						.attr("id", makeId(textContainer, id))
						.addClass("ol-document-text")))
				// Controls container.
				.append($("<li>")
					.addClass("ui-controls-list-item-spaced")
					.append($("<ul>")
						.addClass("ui-sidebyside-controls-list")
						.attr("id", makeId(controlsContainer, id))
						// Previous button.
						.append($("<li>")
							.attr("id", makeId(prevButton, id))
							.addClass(navButtonsClass)
							.button({
								disabled: true,
								label: "Previous",
								icons: {
									primary: "ui-icon-circle-triangle-w"
								}
							})
							.click(goBack))
						// Next button.
						.append($("<li>")
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
							.click(goForward))
						// Toggle cache control.
						.append($("<li>")
							.addClass("ui-sidebyside-controls-list-item-spaced")
							.append($("<input type='checkbox' id='" + makeId(bypassCacheToggle, id) + "'/>")
								.attr("checked", me.options.bypassCache)
								.click(function() {
									me.option({ bypassCache: !me.options.bypassCache });
								}))
							.append("<label for='" + makeId(bypassCacheToggle, id) + "'>Bypass Cache</label>"))
						// Smart nouns toggle.
						.append($("<li>")
								.addClass("ui-sidebyside-controls-list-item-spaced")
								.append($("<input type='checkbox' id='" + makeId(smartNounsToggle, id) + "'/>")
									.attr("checked", me.options.featureType.toLowerCase() === "smart_nouns")
									.click(function() {
										me.option({ featureType: me.options.featureType.toLowerCase() === "smart_nouns" ? "nouns" : "smart_nouns" });
									}))
								.append("<label for='" + makeId(smartNounsToggle, id) + "'>Use Smart Nouns</label>"))))
				.hide();
		},
		
		_init: function() {
			var me = this;
			var id = me._id;
			
			$(me._container).show()
			
			if (!me.options.uuids) {
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
			} else if (key === "featureType") {
				this.refresh();
			}
		},
		
		destroy: function() {
			$(this._container).remove();
			this._container = null;
			
			$.Widget.prototype.destroy.call(this);
		}
	});
	
})(window, window.document, window.jQuery, window.ontologyLearner.UrlStore, window.ontologyLearner.Util);