(function(window, document, $, routes, Utils) {

	var headerContainer = "dw_headerContainer";
	var headerLabel = "dw_headerLabel";
	var textContainer = "dw_textContainer";
	var countContainer = "dw_countContainer";
	var countSpan = "dw_countSpan";
	var controlsContainer = "dw_controlsContainer";
	var nextButton = "dw_nextButton";
	var prevButton = "dw_prevButton";
	var bypassCacheToggle = "dw_bypassCacheToggle";
	var smartNounsToggle = "dw_smartNounsToggle";
	
	var unseenWordClass = "ol-unseen-word";
	var seenWordClass = "ol-seen-word";
	var seenKeywordClass = "ol-seen-keyword";
	
	// Helpers to make things easier.
	var makeId = function(domIdPrefix, id) {
		return domIdPrefix + "_" + id;
	}
	var $$ = function(domIdPrefix, id) {
		return "#" + makeId(domIdPrefix, id);
	};
	
	$.widget("widgets.documentBrowser", {
		options: {
			uuid: null,
			header: "Review",
			collection: null,
			offset: 0,
			bypassCache: false,
			featureType: "nouns",
			showNav: true,
			showCounter: true,
			documentContainerClass: "ol-document-text",
			navButtonsClass: "ui-icon-button"
		},
		
		_id: null,
		
		_container: null,
		
		_refreshCountDisplay: function() {
			var me = this;
			
			if (!me.options.showCounter) {
				return;
			}
			
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
										if (index >= 1 && index <= me.options.collection.seenItems.length) {
											me.option({ offset: index-1 });
										}
									} else {
										return !window.isNaN(String.fromCharCode(event.which));
									}
								}));
						$(event.target).find(".ol-counter-textbox").focus();
					}))
				.append(" of " + me.options.collection.size.toString());
		},
		
		refresh: function() {
			var me = this;
			var id = me._id;
			var collection = me.options.collection || {};
			var uuids = collection.seenItems || [];
			var index = me.options.offset;

			function displayDocument(item, params) {
				$(me._container).find($$(textContainer, id)).spinner();
				$.getJSON(routes.Documents.single({
						collection: collection.uuid,
						document: item.document 
					}), params || {})
					.success(function(document) {
						var text = document.text;
						var match = null;
						
						text = text.replace(/\\{(.+?)}\\/g, function(str, p1) {
							var token = $.parseJSON(p1);
							
							var css;
							if (token.aspect) {
								css = seenKeywordClass;
							} else if (token.isSeen) {
								css = seenWordClass;
							} else {
								css = unseenWordClass;
							}

							var span = $("<span>")
								.text(token.content)
								.attr("lemma", token.lemma)
								.addClass(css);
							
							span = $("<span>")
								.attr("title",
										token.aspect ? ("Already added to the '" + token.aspect + "' aspect") :
											(token.isSeen ? "Already seen and ignored" : "Click to add to the selected aspect"))
								.addClass("ol-feature-element")
								.append(span);
							
							return $("<div>")
								.append(span)
								.html();
						});

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
								me._trigger("featureClick", event, {
									label: $(event.target).attr("lemma") ||$(event.target).text() 
								});
							});
						
						if (index >= uuids.length && index <= collection.size-1) {
							me.options.offset = index = uuids.length;
							uuids.push(item.uuid);
						}
						
						me._refreshCountDisplay();
						
						if (me.options.showNav) {
							// The back or forward buttons might need to be disabled.
							$(me._container).find($$(prevButton, id)).button("option", {
								disabled: (index === 0)
							});
		
							$(me._container).find($$(nextButton, id)).button("option", {
								disabled: (index === me.options.collection.size-1)
							});
						}
					});
			}

			var params = {
				featureType: me.options.featureType,
				bypassCache: me.options.bypassCache
			};
			
			// If a particular uuid is set, use it. If nothing is available, then just go away.
			if (me.options.uuid) {
				displayDocument({
					document: me.options.uuid
				}, params);
				return;
			} else if (!collection.size) {
				return;
			}
			
			// If no seen items available, get them.
			if (!collection.seenItems) {
				$(me._container).find($$(textContainer, id)).spinner();
				$.getJSON(routes.OpinionCollections.Items.seen({ collection: collection.uuid }), { bypassCache: me.options.bypassCache })
					.success(function(uuids) {
						collection.seenItems = uuids;
						$(me._container).find($$(textContainer, id)).spinner("destroy");
						me.option({ offset: uuids.length });
					});
				
				return true;
			}
			
			if (index < 0) {
				return;
			}
			
			if (index >= collection.size) {
				me.options.offset = index = collection.size-1;
			}
			
			// Decide whether we need the next best document or a document by uuid.
			var url = (index >= uuids.length && index < collection.size) ? routes.OpinionCollections.Items.nextBest({ collection: collection.uuid }) :
				routes.OpinionCollections.Items.single({ collection: collection.uuid, item: uuids[index] });
			
			// Get the document and display it.
			$(me._container).find($$(textContainer, id)).spinner();
			$.getJSON(url, params)
				.success(function(item) {
					$(me._container).find($$(textContainer, id)).spinner("destroy");
					displayDocument(item, params);
				});
		},
	
		_create: function() {
			var me = this;
			var id = me._id = window.Math.floor(window.Math.random() * 1000000).toString();
			
			// What to do before we leave a document.
			function leaveDocument(callback) {
				return function() {
					var uuid = me.options.collection.seenItems[me.options.offset];
					var params = {
						bypassCache: me.options.bypassCache	
					};

					$(me._container).find($$(textContainer, id)).spinner();
					$.post(routes.OpinionCollections.Items.single({
							collection: me.options.collection.uuid,
							item: uuid
						}), params)
						.success(function() {
							$(me._container).find($$(textContainer, id)).spinner("destroy");
							callback();
						});
					
//					if (me.options.offset >= me.options.collection.seenItems.length-1) {
//						$(me._container).find($$(textContainer, id)).spinner();
//						$.post(routes.OpinionCollections.Items.single({
//								collection: me.options.collection.uuid,
//								item: uuid
//							}), params)
//							.success(function() {
//								$(me._container).find($$(textContainer, id)).spinner("destroy");
//								callback();
//							});
//					} else {
//						callback();
//					}
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
						.attr("id", makeId(headerContainer, id))
						.append($("<span>")
							.attr("id", makeId(headerLabel, id))
							.text(me.options.header))
						.append($("<span>")
							.attr("id", makeId(countContainer, id))
							.css("float", "right"))))
				// The text container.
				.append($("<li>")
					.append($("<div>")
						.addClass("ui-widget-content")
						.attr("id", makeId(textContainer, id))
						.addClass(me.options.documentContainerClass)))
				// Controls container.
				.append(me.options.showNav && $("<li>")
					.addClass("ui-controls-list-item-spaced")
					.append($("<ul>")
						.addClass("ui-sidebyside-controls-list")
						.attr("id", makeId(controlsContainer, id))
						// Previous button.
						.append($("<li>")
							.attr("id", makeId(prevButton, id))
							.attr("title", "Previous review")
							.addClass(me.options.navButtonsClass)
							.button({
								disabled: true,
								text: false,
								icons: {
									primary: "ui-icon-triangle-1-w"
								}
							})
							.click(leaveDocument(goBack)))
						// Next button.
						.append($("<li>")
							.attr("id", makeId(nextButton, id))
							.attr("title", "Next review")
							.addClass(me.options.navButtonsClass)
							.addClass("ui-sidebyside-controls-list-item-spaced")
							.button({
								disabled: true,
								text: false,
								icons: {
									primary: "ui-icon-triangle-1-e"
								}
							})
							.click(leaveDocument(goForward)))))
				.hide();
		},
		
		_init: function() {
			var me = this;
			var id = me._id;
			
			var params = {
				featureType: me.options.featureType,
				bypassCache: me.options.bypassCache
			};
			
			$(me._container).show()
			me.options.offset = 0;

			// If no collection set, then we try to get the first collection.
			if (me.options.uuid) {
				me.option({ uuid: me.options.uuid });
			} else if (!me.options.collection) {
//				$(me._container).find($$(textContainer, id)).spinner();
//				
//				$.getJSON(routes.OpinionCollections.list(), params)
//					.success(function(collections) {
//						$(me._container).find($$(textContainer, id)).spinner("destroy");
//						if (collections.length) {
//							me.option({ collection: collections[0] });
//						}
//					});
				return;
			} else {
				me.option({ collection: me.options.collection });
			}
		},
		
		_setOption: function(key, value) {
			$.Widget.prototype._setOption.apply(this, arguments);
			
			var me = this;
			var id = this._id;
			
			if (key === "header") {
				$(me._container).find($$(headerLabel, id)).text(value);
			} else if (key == "collection") {
				// If the collection object has a list of seen items, we move to the end of the list, otherwise, refresh will get the list.
				if (!me.options.collection.seenItems) {
					me.refresh();
				} else {
					me.option({ offset: me.options.collection.seenItems.length });
				}
			} else if (key === "offset") {
				me.refresh();
			} else if (key === "bypassCache") {
				me.refresh();
			} else if (key === "featureType") {
				me.refresh();
			} else if (key === "uuid") {
				me.refresh();
			}
		},
		
		destroy: function() {
			$(this._container).remove();
			this._container = null;
			
			$.Widget.prototype.destroy.call(this);
		}
	});
	
})(window, window.document, window.jQuery, window.obome.routes, window.obome.Utils);