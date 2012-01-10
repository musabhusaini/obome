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
			.val(options.value);
		
		$.each(options.handlers || [], function(i, handlerInfo) {
			$(listItem).on(handlerInfo.event, handlerInfo.handler);
		});
		
		$(listItem).on("dblclick", function(event) {
			showSingleFieldDialog({
				operation: "Edit",
				buttonTitle: "Update",
				fieldName: options.typeName,
				operate: function(newValue) {
					options.updateHandler($(event.target).val(), newValue)
						.success(function() {
							$(event.target).text(newValue).val(newValue);
						})
						.error(function() {
							Util.displayMessage({
								title: 'Update Failed',
								message: "Could not update, possibly due to a conflict."
							});
						})
				}
			});
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
									options.addAjax(value)
										.success(function() {
											options.addDisplayItem(value);
										})
										.error(function() {
											Util.displayMessage({
												title: 'Add Failed',
												message: "Could not add, possibly due to a conflict."
											});
										});
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
							options.deleteAjax(value)
								.success(function() {
									options.deleteDisplayItem(value);
								})
								.error(function() {
									Util.displayMessage({
										title: 'Delete Failed',
										message: "Could not delete, possibly due to a conflict."
									});									
								});
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
		$(option).attr("selected", "selected").change();
	}

	// Create the widget.
	$.widget("widgets.aspectsBrowser", {
		options: {
			aspects: []
		},
		
		_id: null,
		
		_container: null,
		
		_deleteDisplayAspect: function(aspect) {
			var aspectsList = $$(aspectsListId, this._id);
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
				$.each($$(keywordsListId, this._id).find("option").val(), function(i, keyword) {
					this._deleteDisplayKeyword(keyword);
				});
				
				$($(aspectsList).data("deleteButton")).button("disable");
			}
		},
		
		_deleteDisplayKeyword: function(keyword) {
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
		},
		
		_addDisplayAspect: function(aspect) {
			aspect = convertToAspects([aspect])[0];
			
			var me = this;
			var aspectsList = $$(aspectsListId, me._id); 
			var newAspect = $(createEditableListItem({
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
		},
		
		_addDisplayKeyword: function(keyword) {
			var keywordsList = $$(keywordsListId, this._id);
			var newKeyword = $(createEditableListItem({
				text: keyword,
				value: keyword,
				typeName: "Keyword",
				updateHandler: function(keyword, newValue) {
					var aspect = $$(aspectsListId, this._id).val();
					return $.post(UrlStore.postKeyword(aspect, keyword), {
						value: newValue,
					});
				}
			}))
			.appendTo(keywordsList);
			
			if (!$(keywordsList).find(":selected").size()) {
				selectOption(newKeyword);
			}
		},

		refresh: function() {
			var me = this;
			
			$$(aspectsListId, me._id).empty();
			
			$.each(me.options.aspects, function(i, aspect) {
				me._addDisplayAspect(aspect);
			});			
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
				
				$(event.target).attr("disabled", "disabled");
				selected = $(event.target).val();
				$$(keywordsListId, id).empty();
				$.getJSON(UrlStore.getAspect(selected), function(keywords) {
					$.each(keywords, function(i, keyword) {
						me._addDisplayKeyword(keyword);
					});
					$(event.target).removeAttr("disabled");
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
				addAjax: function(value) {
					return $.post(UrlStore.postAspect(value));
				},
				addDisplayItem: function(value) {
					me._addDisplayAspect(value);
				},
				deleteAjax: function(value) {
					return $.ajax({
						type: "DELETE",
						url: UrlStore.deleteAspect(value)
					});
				},
				deleteDisplayItem: function(value) {
					me._deleteDisplayAspect(value);
				}
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
				addAjax: function(value) {
					return $.post(UrlStore.postKeyword($$(aspectsListId, id).val(),
							value));
				},
				addDisplayItem: function(value) {
					me._addDisplayKeyword(value);
				},
				deleteAjax: function(value) {
					var aspect = $$(aspectsListId, me._id).val();
					return $.ajax({
						type: "DELETE",
						url: UrlStore.deleteKeyword(aspect, value)
					});					
				},
				deleteDisplayItem: function(value) {
					me._deleteDisplayKeyword(value);
				}
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