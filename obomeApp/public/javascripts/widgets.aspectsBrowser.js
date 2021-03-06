(function(window, document, JSON, $, routes, Utils) {

	var aspectsListId = "ab_aspects_list";
	var keywordsListId = "ab_keywords_list";
	
	var olButtonClass = "ui-icon-button";
	
	// Helpers to make things easier.
	function makeId(domIdPrefix, id) {
		return domIdPrefix + "_" + id;
	}
	
	function $$(domIdPrefix, id) {
		return $("#" + makeId(domIdPrefix, id));
	}

	function callAndDisplay(handlers, value, errorMessage, context, list) {
		if (!value) {
			Utils.displayMessage({
				title: "No Value",
				message: "Nothing to do anything with."
			});
			return;
		}
		
		context = context || this;
		$.proxy(handlers.ajax, context)(value)
			.success(function(value) {
				$.proxy(handlers.display, context)(value);
				
				if (list) {
					$(list).effect("highlight", {}, 1000);
				}
			})
			.error(function() {
				Utils.displayMessage(errorMessage);
			});
	}
	
	// Creates the dialog div.
	function createDialogContainer(options) {
		var field_id = options.fieldName.toLowerCase() + "-field";
		var form = $("<div>")
			.attr("title", options.title)
			.append($("<form>")
				.append($("<fieldset>")
					.append($("<label>")
							.attr("for", field_id)
							.text(options.fieldName + ":")
					.append($("<input>")
						.addClass("ui-widget-content")
						.addClass("ui-corner-all")
						.attr("type", "text")
						.attr("id", field_id)
						.attr("name", field_id)
						.val(options.initialValue || "")
						))));
		
		return form;
	}

	// Shows a single field modal dialog.
	function showSingleFieldDialog(options) {
		var dialogOptions = options;
		dialogOptions.buttonTitle = dialogOptions.buttonTitle || dialogOptions.operation;
		var form = createDialogContainer({
			title: dialogOptions.operation + " " + dialogOptions.fieldName,
			fieldName: dialogOptions.fieldName,
			initialValue: dialogOptions.initialValue
		});
		
		function closeDialog() {
			$(this).dialog("close");
			$(form).remove();
		}
		
		var buttonsOption = {};
		
		function submit() {
			var value = {
				label: $(form).find("input[type='text'][id|='" + dialogOptions.fieldName.toLowerCase() + "']").val()
			};
			window.setTimeout(function() {
				dialogOptions.operate(value);
			}, 0);
			closeDialog();			
		}
		
		buttonsOption[dialogOptions.buttonTitle] = submit;
		buttonsOption.Cancel = function() {
			closeDialog();
		};
		
		$(form)
			.appendTo($(document))
			.dialog({
				autoOpen: true,
				modal: true,
				buttons: buttonsOption,
				resizable: false,
				open: function() {
					$(form)
						.submit(function() {
							submit();
							return false;
						});
				}
			});
		
		$(form).parent().find("button:first").button("disable");
		$(form).find("input[type='text'][id|='" + dialogOptions.fieldName.toLowerCase() + "']")
			.keyup(function(event) {
				if (!$(event.target).val()) {
					$(form).parent().find("button:first").button("disable");
				} else {
					$(form).parent().find("button:first").button("enable");
				}
			});
	}
	
	// Creates an item in an editable list.
	function createEditableListItem(options) {
		var listItem = $("<option>")
			.attr("title", "Double click to edit")
			.addClass("ui-list-item")
			.text(options.text)
			.val(options.value)
			.hover(function(event) {
				$(event.target).addClass("ui-state-highlight", "fast");
			}, function(event) {
				$(event.target).removeClass("ui-state-highlight", "fast");
			})
			.dblclick(function(event) {
				var uuid = $(event.target).val();
				showSingleFieldDialog({
					operation: "Edit",
					buttonTitle: "Update",
					fieldName: options.typeName,
					initialValue: $(event.target).text(),
					operate: function(newValue) {
						callAndDisplay({
							ajax: function(value) {
								value.uuid = uuid;
								return options.updateHandler({
									uuid: uuid,
									label: $(event.target).text()
								}, value);
							},
							
							display: function(value) {
								$(event.target).text(value.label);
							}
						}, newValue, {
							title: 'Update Failed',
							message: "This " + options.typeName.toLowerCase() + " already exists."
						}, options.me);
					}
				});
			});

		$.each(options.handlers || [], function(i, handlerInfo) {
			$(listItem).on(handlerInfo.event, handlerInfo.handler);
		});
		
		return listItem;
	}
	
	// Creates an editable list.
	function createEditableList(options) {
		var me = options.me;
		var list = null;
		var addButton = null;
		var deleteButton = null;
		var listContainer = $("<ul>")
			.addClass("ui-controls-list")
			// header.
			.append($("<li>")
				.append($("<div>")
					.addClass("ui-widget-header")
					.addClass("ui-corner-top")
					.addClass("ol-header")
					.text(options.header)))
			// listbox.
			.append($("<li>")
				.append(list = $("<select>")
					.addClass("ol-list")
					.addClass("ui-widget-content")
					.attr("id", options.listId)
					.attr("size", options.listSize))
					.change(function(event) {
						(options.changeHandler || function() { })(event);
						if ($(list).find(":selected").size()) {
							$($(list).data("deleteButton")).button("enable");
						} else {
							$($(list).data("deleteButton")).button("disable");
						}
					})
					.droppable({
						hoverClass: "ui-state-highlight",
						greedy: true,
						scope: "features",
						drop: function(event, ui) {
							var text = $(ui.draggable).find(".doc-seen, .doc-keyword").eq(0).data("lemma") || $(ui.draggable).text();
							callAndDisplay(options.addHandlers, {
								label: text
							}, {
								title: "Add Failed",
								message: "This " + options.typeName.toLowerCase() + " already exists."
							}, me, list);
						}
					}))
			// add buttons.
			.append($("<li>")
				.addClass("ui-controls-list-item-spaced")
				.append($("<ul>")
					.addClass("ui-sidebyside-controls-list")
					.append(addButton = $("<li>")
						.attr("title", "Add new " + options.typeName.toLowerCase())
						.addClass(olButtonClass)
						.button({
							text: false,
							disabled: true,
							icons: {
								primary: "ui-icon-plusthick"
							}
						})
						.click(function() {
							showSingleFieldDialog({
								operation: "Add",
								buttonTitle: "Add",
								fieldName: options.typeName,
								operate: function(value) {
									callAndDisplay(options.addHandlers, value, {
										title: "Add Failed",
										message: "This " + options.typeName.toLowerCase() + " already exists."
									}, me, list);
								}
							});
						}))
					.append(deleteButton = $("<li>")
						.attr("title", "Delete this " + options.typeName.toLowerCase())
						.addClass("ui-sidebyside-controls-list-item-spaced")
						.addClass(olButtonClass)
						.button({
							text: false,
							disabled: true,
							icons: {
								primary: "ui-icon-trash"
							}
						})
						.click(function() {
							var value = $(list).val();
							callAndDisplay(options.deleteHandlers, value, {
								title: 'Delete Failed',
								message: "Unexpected error: could not delete."
							}, me, list);
						}))));
		
		// Set some data for buttons that we can use later.
		$(list)
			.data("addButton", addButton)
			.data("deleteButton", deleteButton);
		
		return listContainer;
	}

	// Selects a given option in a list properly.
	function selectOption(option) {
		$(option).attr("selected", true).change();
	}

	// Create the widget.
	$.widget("widgets.aspectsBrowser", {
		options: {
			collection: null,
			bypassCache: false
		},
		
		_id: null,
		
		_container: null,

		_addAspectHandlers: {
			ajax: function(aspect) {
				return $.post(routes.Aspects.single({
					collection: this.options.collection.uuid,
					aspect: "new"
				}), window.JSON.stringify(aspect));
			},
			
			display: function(aspect) {
				var me = this; 
				var newAspectElem;
				
				var aspectsList = $$(aspectsListId, me._id)
					.append(newAspectElem = $(createEditableListItem({
						me: me,
						text: aspect.label,
						value: aspect.uuid,
						typeName: "Aspect",
						updateHandler: function(oldAspect, newAspect) {
							return $.post(routes.Aspects.single({
								collection: me.options.collection.uuid,
								aspect: oldAspect.uuid
							}), window.JSON.stringify(newAspect));
						}
					})));

				selectOption(newAspectElem);
			}
		},
		
		_deleteAspectHandlers: {
			ajax: function(aspect) {
				return $.ajax({
					type: "DELETE",
					url: routes.Aspects.single({
						collection: this.options.collection.uuid,
						aspect: aspect.uuid || aspect
					})
				});
			},
			
			display: function(aspect) {
				var me = this;
				var aspectsList = $$(aspectsListId, me._id);
				var toDelete = $(aspectsList).find("option[value='" + (aspect.uuid || aspect) + "']");
				
				// Find the next possible selection.
				var next = $(toDelete).next();
				if (!$(next).size()) {
					next = $(toDelete).prev();
				}
				selectOption(next);
				
				$(toDelete).remove();
				if (!$(next).size()) {
					// If this was the last aspect, then clear keywords and disable useless buttons.
					$.each($$(keywordsListId, me._id).find("option").val() || [], function(i, keyword) {
						$.proxy(me._deleteKeywordHandlers.display, me)(keyword);
					});
					
					$($(aspectsList).data("deleteButton")).button("disable");
					
					var keywordsList = $$(keywordsListId, me._id);
					$($(keywordsList).data("addButton")).button("disable");
					$($(keywordsList).data("deleteButton")).button("disable");
					$(keywordsList).empty();
				}
			}
		},
		
		_addKeywordHandlers: {
			ajax: function(keyword) {
				return $.post(routes.Keywords.single({
					collection: this.options.collection.uuid,
					aspect: $$(aspectsListId, this._id).val() || null,
					keyword: "new"
				}), window.JSON.stringify(keyword));
			},
			
			display: function(keyword) {
				var me = this;
				var newKeywordElem;
				
				var keywordsList = $$(keywordsListId, me._id)
					.append(newKeywordElem = $(createEditableListItem({
						me: me,
						text: keyword.label,
						value: keyword.uuid,
						typeName: "Keyword",
						updateHandler: function(oldKeyword, newKeyword) {
							var aspect = $$(aspectsListId, me._id).val() || null;
							return $.post(routes.Keywords.single({
								collection: me.options.collection.uuid,
								aspect: aspect,
								keyword: oldKeyword.uuid
							}), window.JSON.stringify(newKeyword));
						}
					})));

				selectOption(newKeywordElem);
			}
		},
		
		_deleteKeywordHandlers: {
			ajax: function(keyword) {
				var aspect = $$(aspectsListId, this._id).val() || null;
				return $.ajax({
					type: "DELETE",
					url: routes.Keywords.single({
						collection: this.options.collection.uuid,
						aspect: aspect,
						keyword: keyword.uuid || keyword
					})
				});					
			},
			
			display: function(keyword) {
				var keywordsList = $$(keywordsListId, this._id);
				var toDelete = keywordsList.find("option[value='" + (keyword.uuid || keyword) + "']");
				var next = $(toDelete).next();
				if (!$(next).size()) {
					next = $(toDelete).prev();
				}
				
				selectOption(next);
				
				$(toDelete).remove();
				
				if (!$(next).size()) {
					$($(keywordsList).data("deleteButton")).button("disable");
				}
			}
		},
		
		addAspect: function(aspect) {
			var me = this;
			callAndDisplay(me._addAspectHandlers, aspect, {
				title: "Add Failed",
				message: "This aspect already exists."
			}, me, $$(aspectsListId, me._id));
		},
		
		addKeyword: function(keyword) {
			var me = this;
			callAndDisplay(me._addKeywordHandlers, keyword, {
				title: "Add Failed",
				message: "This keyword already exists."
			}, me, $$(keywordsListId, me._id));
		},
		
		refresh: function() {
			var me = this;
			
			var aspectsList = $$(aspectsListId, me._id);
			var keywordsList = $$(keywordsListId, me._id);
			$(aspectsList)
				.attr("disabled", true)
				.empty();
			$(keywordsList)
				.attr("disabled", true)
				.empty();
			
			$.each(me.options.collection.aspects, function(i, aspect) {
				$.proxy(me._addAspectHandlers.display, me)(aspect);
			});
			
			selectOption($(aspectsList).find("option").eq(0));
			
			$($(aspectsList)
				.removeAttr("disabled")
				.data("addButton"))
					.button("enable");
			$(keywordsList)
				.removeAttr("disabled");
		},
	
		_create: function() {
			var me = this;
			var id = me._id = window.Math.floor(window.Math.random() * 1000000).toString();
			
			var selected = null;
			function changeAspect(event) {
				// We don't want to do anything if the selection hasn't actually changed.
				if (selected && selected === $(event.target).val()) {
					return;
				}
				
				var target = event.target;
				if ($(target).is("option")) {
					target = $(target).parent("select");
				}

				selected = $(target)
					.attr("disabled", true)
					.val();
				
				var keywordsList = $$(keywordsListId, me._id);
				$($(keywordsList)
					.attr("disabled", true)
					.empty()
					.data("deleteButton"))
						.button("disable");
				
				$($(keywordsList)
					.data("addButton"))
						.button("enable");
				
				$.getJSON(routes.Keywords.list({
					collection: me.options.collection.uuid,
					aspect: selected
				}), function(keywords) {
					$.each(keywords || [], function(i, keyword) {
						$.proxy(me._addKeywordHandlers.display, me)(keyword);
					});
					
					// Select the fist one.
					selectOption($(keywordsList).find("option").eq(0));
					
					$(target)
						.removeAttr("disabled");
					$(keywordsList)
						.removeAttr("disabled");
				});
			}
			
			var aspectsContainer;
			var keywordContainer;
			
			// Define the container that will keep everything else.
			me._container = $("<div>")
				.css("width", "100%")
				.addClass("ui-widget")
				.append($("<div>")
					.css("float", "left")
					.css("width", "50%")
					.append($("<div>")
						.css("padding-right", ".5em")
						.append(aspectsContainer = $(createEditableList({
							me: me,
							id: id,
							typeName: "Aspect",
							header: "Aspects",
							listId: makeId(aspectsListId, id),
							listSize: 20,
							changeHandler: changeAspect,
							addHandlers: me._addAspectHandlers,
							deleteHandlers: me._deleteAspectHandlers
						})))))
				.append($("<div>")
					.css("float", "right")
					.css("width", "50%")
					.append(keywordContainer = $(createEditableList({
						me: me,
						id: id,
						typeName: "Keyword",
						header: "Keywords",
						listId: makeId(keywordsListId, id),
						listSize: 20,
						addHandlers: me._addKeywordHandlers,
						deleteHandlers: me._deleteKeywordHandlers
					}))));
		},
		
		_init: function() {
			var me = this;
			
			$(me.element)
				.append(me._container);
			
			if (!me.options.collection) {
				$(me._container).find($$(textContainer, id)).spinner();
				
				$.getJSON(routes.OpinionCollections.list(), params)
					.success(function(collections) {
						$(me._container).find($$(textContainer, id)).spinner("destroy");
						if (collections.length) {
							me.option({ collection: collections[0] });
						}
					});
			} else {
				me.option({ collection: me.options.collection });
			}
		},
		
		_setOption: function(key, value) {
			$.Widget.prototype._setOption.apply(this, arguments);
			
			var me = this;
			
			if (key === "collection") {
				if (!me.options.collection.aspects) {
					$.getJSON(routes.Aspects.list({
						collection: me.options.collection.uuid
					}), function(aspects) {
						me.options.collection.aspects = aspects || [];
						me.refresh();
					});
				} else {
					me.refresh();
				}
			} else if (key === "bypassCache") {
				me.refresh();
			}
		},
		
		destroy: function() {
			this._container.remove();
			this._container = null;
			
			$.Widget.prototype.destroy.call(this);
		}
	});

})(window, window.document, window.JSON, window.jQuery, window.obome.routes, window.obome.Utils);