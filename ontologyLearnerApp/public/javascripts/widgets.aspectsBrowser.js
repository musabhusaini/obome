(function(window, document, $, UrlStore, Util) {

	var aspectsListId = "ab_aspects_list";
	var keywordsListId = "ab_keywords_list";
	
	// Helpers to make things easier.
	function makeId(domIdPrefix, id) {
		return domIdPrefix + "_" + id;
	}
	
	function $$(domIdPrefix, id) {
		return $("#" + makeId(domIdPrefix, id));
	}

	function callAndDisplay(handlers, value, errorMessage, context) {
		if (!value) {
			Util.displayMessage({
				title: "No Value",
				message: "Nothing to do anything with."
			});
			return;
		}
		
		context = context || this;
		$.proxy(handlers.ajax, context)(value)
			.success(function() {
				$.proxy(handlers.display, context)(value);
			})
			.error(function() {
				Util.displayMessage(errorMessage);
			});
	}
	
	// Creates the dialog div.
	function createDialog(options) {
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
						))));
		
		return form;
	}

	// Shows a single field modal dialog.
	function showSingleFieldDialog(options) {
		var dialogOptions = options;
		dialogOptions.buttonTitle = dialogOptions.buttonTitle || dialogOptions.operation;
		var form = createDialog({
			title: dialogOptions.operation + " " + dialogOptions.fieldName,
			fieldName: dialogOptions.fieldName,
		});
		
		function closeDialog() {
			$(this).dialog("close");
			$(form).remove();
		}
		
		var buttonsOption = {};
		
		function submit() {
			window.setTimeout(dialogOptions.operate($(form).find("input[type='text'][id|='" + dialogOptions.fieldName.toLowerCase() + "']").val()), 0);
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
			.addClass("ui-list-item")
			.text(options.text)
			.val(options.value)
			.hover(function(event) {
				$(event.target).addClass("ui-state-highlight", "fast");
			}, function(event) {
				$(event.target).removeClass("ui-state-highlight", "fast");
			})
			.dblclick(function(event) {
				showSingleFieldDialog({
					operation: "Edit",
					buttonTitle: "Update",
					fieldName: options.typeName,
					operate: function(newValue) {
						callAndDisplay({
							ajax: function(value) {
								return options.updateHandler($(event.target).val(), value);
							},
							
							display: function(value) {
								$(event.target).text(value).val(value);
							}
						}, newValue, {
							title: 'Update Failed',
							message: "Could not update, possibly due to a conflict."
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
					.addClass("ol-editable-list")
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
						drop: function(event, ui) {
							callAndDisplay(options.addHandlers, ui.draggable.text(), {
								title: "Add Failed",
								message: "Could not add, possibly due to a conflict."
							}, me);
						}
					}))
			// add button.
			.append($("<li>")
				.addClass("ui-controls-list-item-spaced")
				.append($("<ul>")
					.addClass("ui-sidebyside-controls-list")
					.append(addButton = $("<li>")
						.button({
							label: "Add"
						})
						.click(function() {
							showSingleFieldDialog({
								operation: "Add",
								buttonTitle: "Add",
								fieldName: options.typeName,
								operate: function(value) {
									callAndDisplay(options.addHandlers, value, {
										title: "Add Failed",
										message: "Could not add, possibly due to a conflict."
									}, me);
								}
							});
						}))
					.append(deleteButton = $("<li>")
						.addClass("ui-sidebyside-controls-list-item-spaced")
						.button({
							label: "Delete",
							disabled: true
						})
						.click(function() {
							var value = $(list).val();
							callAndDisplay(options.deleteHandlers, value, {
								title: 'Delete Failed',
								message: "Could not delete, possibly due to a conflict."
							}, me);
						}))));
		
		// Set some data for buttons that we can use later.
		$(list)
			.data("addButton", addButton)
			.data("deleteButton", deleteButton);
		
		return listContainer;
	}

	// Converts a list of aspects to the proper format.
	function convertToAspects(aspects) {
		var list = [];
		for (var i=0; i<aspects.length; i++) {
			var aspect = aspects[i];
			list.push({
				name: aspect.name || aspect
			});
		}
		
		return list;
	}
	
	// Selects a given option in a list properly.
	function selectOption(option) {
		$(option).attr("selected", true).change();
	}

	// Create the widget.
	$.widget("widgets.aspectsBrowser", {
		options: {
			aspects: []
		},
		
		_id: null,
		
		_container: null,

		_addAspectHandlers: {
			ajax: function(value) {
				return $.post(UrlStore.postAspect(value));
			},
			
			display: function(aspect) {
				aspect = convertToAspects([aspect])[0];
				
				var me = this;
				var aspectsList = $$(aspectsListId, me._id); 
				var newAspect = $(createEditableListItem({
					me: me,
					text: aspect.name,
					value: aspect.name,
					typeName: "Aspect",
					updateHandler: function(aspect, newValue) {
						return $.post(UrlStore.postAspect(aspect), {
							value: newValue
						});
					}
				})).appendTo(aspectsList);
				
				// If nothing is selected, then select this.
				if (!$(aspectsList).find(":selected").size()) {
					selectOption(newAspect);
				}
			}
		},
		
		_deleteAspectHandlers: {
			ajax: function(value) {
				return $.ajax({
					type: "DELETE",
					url: UrlStore.deleteAspect(value)
				});
			},
			
			display: function(aspect) {
				var me = this;
				var aspectsList = $$(aspectsListId, me._id);
				var toDelete = $(aspectsList).find("option[value='" + aspect + "']");
				
				// Find the next possible selection.
				var next = $(toDelete).next();
				if (!$(next).size()) {
					next = $(toDelete).prev();
				}
				selectOption(next);
				
				$(toDelete).remove();
				if (!$(next).size()) {
					// If this was the last aspect, then clear keywords and disable the delete button.
					$.each($$(keywordsListId, me._id).find("option").val() || [], function(i, keyword) {
						$.proxy(me._deleteKeywordHandlers.display, me)(keyword);
					});
					
					$($(aspectsList).data("deleteButton")).button("disable");
				}
			}
		},
		
		_addKeywordHandlers: {
			ajax: function(value) {
				return $.post(UrlStore.postKeyword($$(aspectsListId, this._id).val() || null,
						value));
			},
			
			display: function(keyword) {
				var me = this;
				var keywordsList = $$(keywordsListId, me._id);
				var newKeyword = $(createEditableListItem({
					me: me,
					text: keyword,
					value: keyword,
					typeName: "Keyword",
					updateHandler: function(keyword, newValue) {
						var aspect = $$(aspectsListId, me._id).val() || null;
						return $.post(UrlStore.postKeyword(aspect, keyword), {
							value: newValue,
						});
					}
				}))
				.appendTo(keywordsList);
				
				if (!$(keywordsList).find(":selected").size()) {
					selectOption(newKeyword);
				}
			}
		},
		
		_deleteKeywordHandlers: {
			ajax: function(value) {
				var aspect = $$(aspectsListId, this._id).val() || null;
				return $.ajax({
					type: "DELETE",
					url: UrlStore.deleteKeyword(aspect, value)
				});					
			},
			
			display: function(keyword) {
				var keywordsList = $$(keywordsListId, this._id);
				var toDelete = keywordsList.find("option[value='" + keyword + "']");
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
			callAndDisplay(me._addAspectHandlers, value, {
				title: "Add Failed",
				message: "Could not add, possibly due to a conflict."
			}, me);
		},
		
		addKeyword: function(keyword) {
			var me = this;
			var aspect = $$(aspectsListId, me._id).val() || null;
			callAndDisplay(me._addKeywordHandlers, keyword, {
				title: "Add Failed",
				message: "Could not add, possibly due to a conflict."
			}, me);
		},
		
		refresh: function() {
			var me = this;
			
			me.disable();
			$$(aspectsListId, me._id).empty();
			$.each(me.options.aspects, function(i, aspect) {
				$.proxy(me._addAspectHandlers.display, me)(aspect);
			});
			me.enable();
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
				
				$(target).attr("disabled", true);
				selected = $(target).val();
				$$(keywordsListId, id).empty();
				$.getJSON(UrlStore.getAspect(selected), function(keywords) {
					$.each(keywords, function(i, keyword) {
						$.proxy(me._addKeywordHandlers.display, me)(keyword);
					});
					$(target).removeAttr("disabled");
				});
			}
			
			// Define the container that will keep everything else.
			me._container = $("<ul>")
				.addClass("ui-widget")
				.addClass("ui-sidebyside-controls-list");
			
			var aspectsContainer = $(createEditableList({
				me: me,
				id: id,
				typeName: "Aspect",
				header: "Aspects",
				listId: makeId(aspectsListId, id),
				listSize: 20,
				changeHandler: changeAspect,
				addHandlers: me._addAspectHandlers,
				deleteHandlers: me._deleteAspectHandlers
			})
			.appendTo($("<li>")
					.appendTo(me._container)));
			
			var keywordContainer = $(createEditableList({
				me: me,
				id: id,
				typeName: "Keyword",
				header: "Keywords",
				listId: makeId(keywordsListId, id),
				listSize: 20,
				addHandlers: me._addKeywordHandlers,
				deleteHandlers: me._deleteKeywordHandlers
			})
			.appendTo($("<li>")
				.addClass("ui-sidebyside-controls-list-item-spaced")
				.appendTo(me._container)));
		},
		
		_init: function() {
			var me = this;
			
			$(me._container)
				.appendTo(me.element);
			
			function setAspects(aspects) {
				me.option("aspects", aspects);
			};
			
			if (!me.options.aspects || !me.options.aspects.length) {
				$.getJSON(UrlStore.getAspectList(), function(aspects) {
					setAspects(aspects);
				});
			} else {
				setAspects(me.options.aspects);
			}
		},
		
		_setOption: function(key, value) {
			$.Widget.prototype._setOption.apply(this, arguments);
			
			if (key === "aspects" && value.length >= 0) {
				this.options.aspects = convertToAspects(value);
				this.refresh();
			}
		},
		
		destroy: function() {
			this._container.remove();
			this._container = null;
			
			$.Widget.prototype.destroy.call(this);
		}
	});

})(window, window.document, window.jQuery, window.ontologyLearner.UrlStore, window.ontologyLearner.Util);