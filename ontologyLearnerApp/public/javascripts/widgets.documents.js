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
			totalDocuments: -1,
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
									} else {
										return !window.isNaN(String.fromCharCode(event.which));
									}
								}));
						$(event.target).find(".ol-counter-textbox").focus();
					}))
				.append(" of " + me.options.totalDocuments.toString());
		},
		
		refresh: function() {
			var me = this;
			var id = me._id;
			var uuids = me.options.uuids;
			var index = me.options.offset;
			
			if (index < 0) {
				return;
			}
			
			var url = index >= uuids.length ? UrlStore.getNextBestDocument(me.options.featureType, me.options.bypassCache) :
				UrlStore.getDocument(uuids[index], me.options.featureType, me.options.bypassCache);
			
			$(me._container).find($$(textContainer, id)).spinner();
			$.getJSON(url, function(doc) {
				var pattern = /\\feature\{(.+?)\}/mg;
				var text = doc.text;
				var match = null;
				text = text.replace(pattern, "<span class='ol-feature-element'>$1</span>");
				
				$(me._container).find($$(textContainer, id)).spinner("destroy");
				$(me._container)
					.find($$(textContainer, id))
					.empty()
					.html(window.unescape(text));
								
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
				
				if (index >= uuids.length) {
					me.options.offset = uuids.length;
					uuids.push(doc.uuid);
				}
				
				me._refreshCountDisplay();
			});

			$(me._container).find($$(prevButton, id)).button("option", {
				disabled: (index === 0)
			});

			$(me._container).find($$(nextButton, id)).button("option", {
				disabled: (index === me.options.totalDocuments-1)
			});
		},
	
		_create: function() {
			var me = this;
			var id = me._id = window.Math.floor(window.Math.random() * 1000000).toString();
			
			function leaveDocument(callback) {
				return function() {
					var uuid = me.options.uuids[me.options.offset];
					$.post(UrlStore.seeDocument(uuid));
					callback();
				}
			}
			
			function goForward() {
				me.option({ offset: me.options.offset+1 });
			}
			
			function goBack() {
				me.option({ offset: me.options.offset-1 });
			}
			
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
							.click(leaveDocument(goBack)))
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
							.click(leaveDocument(goForward)))
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
			me.options.offset = 0;
			
			if (me.options.totalDocuments < 0 || !me.options.uuids || me.options.totalDocuments < me.options.uuids.length) {
				$(me._container).find($$(textContainer, id)).spinner();
				$.getJSON(UrlStore.getDocumentList(), function(clusterHeads) {
					$(me._container).find($$(textContainer, id)).spinner("destroy");
					
					me.options.totalDocuments = clusterHeads.seen.length + clusterHeads.unseen.length;
					me.options.uuids = clusterHeads.seen;
					me.options.offset = clusterHeads.seen.length;
					me.refresh();
				});
			} else {
				me.refresh();
			}
		},
		
		_setOption: function(key, value) {
			$.Widget.prototype._setOption.apply(this, arguments);
			
			if (key === "header") {
				$(this._container).find($$(headerLabel, this._id)).text(value);
			} else if (key === "uuids" || key === "offset" || key === "totalDocuments") {
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