(function(window, document, $, UrlStore) {

	var aspectsHeaderId = "ab_aspects_header";
	var aspectsListId = "ab_aspects_list";
	var aspectsTextboxId = "ab_aspects_textbox";
	var aspectsAddButtonId = "ab_aspects_add_button";
	var keywordsHeaderId = "ab_keywords_header";
	var keywordsListId = "ab_keywords_list";
	var keywordsTextboxId = "ab_keywords_textbox";
	var keywordsAddButtonId = "ab_keywords_add_button";
	
	// Helpers to make things easier.
	var makeId = function(domIdPrefix, id) {
		return domIdPrefix + "_" + id;
	}
	
	var $$ = function(domIdPrefix, id) {
		return "#" + makeId(domIdPrefix, id);
	};
	
	var createEditableListItem = function(options) {
		return $("<option>")
			.addClass("ui-list-item")
			.text(options.text)
			.val(options.value)
			.click(options.handler);
	};
	
	var createEditableList = function(options) {
		var me = options.me;
		var list = null;
		var listContainer = $("<ul>")
			.addClass("ui-controls-list")
			// header.
			.append($("<li>")
				.append($("<div>")
					.attr("id", options.headerId)
					.addClass("ui-widget-header")
					.addClass("ui-corner-top")
					.addClass("ol-header")
					.text(options.header)))
			// listbox.
			.append($("<li>")
				.append(list = $("<select>")
					.addClass("ol-editable-list")
					.addClass("ui-widget-content")
					.attr("id", options.listId, options.id)
					.attr("size", options.listSize)))
			// controls.
			.append($("<li>")
				.addClass("ui-controls-list-item-spaced")
				.append($("<ul>")
					.addClass("ui-controls-list")
					// textbox.
					.append($("<li>")
						.append($("<input type='text'>")
							.addClass("ol-edit-textbox")
							.attr("id", options.textboxId)))
					// buttons.
					.append($("<li>")
						.addClass("ui-controls-list-item-spaced")
						.append($("<ul>")
							.addClass("ui-sidebyside-controls-list")
							// add button.
							.append($("<li>")
								.attr("id", options.buttonId)
								.button({
									disabled: true,
									label: "Add"
								})
								.click(options.buttonClickHandler))
							.append($("<li>")
								.addClass("ui-sidebyside-controls-list-item-spaced")
								.button({
									disabled: true,
									label: "Cancel"
								})
								.click(function() {}))))));

		$.each(options.list, function(i, item) {
			$(createEditableListItem({
				text: item,
				value: item,
				handler: options.itemsClickHandler
			})).appendTo(list);
		});
		
		return listContainer;
	};
	
	$.widget("widgets.aspectsBrowser", {
		options: {
			aspects: [],
			aspectsHeader: "Aspects",
			keywordsHeader: "Keywords"
		},
		
		_id: null,
		
		_container: null,
		
		refresh: function() {
		},
	
		_create: function() {
			var me = this;
			var id = me._id = window.Math.floor(window.Math.random() * 1000000).toString();
			
			// Define the container that will keep everything else.
			me._container = $("<ul>")
				.addClass("ui-widget")
				.addClass("ui-sidebyside-controls-list");

			var list = [];
			for (var i=0; i<me.options.aspects.length; i++) {
				var aspect = me.options.aspects[i];
				list.push(aspect.name || aspect);
			}
			
			var aspectsContainer = $(createEditableList({
				me: me,
				id: id,
				headerId: makeId(aspectsHeaderId, id),
				header: me.options.aspectsHeader,
				listId: makeId(aspectsListId, id),
				list: list,
				listSize: 20,
				itemsClickHandler: function(event) {
					
				},
				textboxId: makeId(aspectsTextboxId, id),
				buttonId: makeId(aspectsAddButtonId, id),
				buttonClickHandler: function(event) {
					
				}
			})
			.appendTo($("<li>")
					.appendTo(me._container)));
			
			var keywordContainer = $(createEditableList({
				me: me,
				id: id,
				headerId: makeId(keywordsHeaderId, id),
				header: me.options.keywordsHeader,
				listId: makeId(keywordsListId, id),
				list: [],
				listSize: 20,
				itemsClickHandler: function(event) {
					
				},
				textboxId: makeId(keywordsTextboxId, id),
				buttonId: makeId(keywordsAddButtonId, id),
				buttonClickHandler: function(event) {
					
				}
			})
			.appendTo($("<li>")
					.addClass("ui-sidebyside-controls-list-item-spaced")
					.appendTo(me._container)));
			
			$(me._container)
				.hide();
		},
		
		_init: function() {
			$(this._container)
				.appendTo(this.element)
				.show();
		},
		
		_setOption: function(key, value) {
			$.Widget.prototype._setOption.apply(this, arguments);
		},
		
		destroy: function() {
			$.Widget.prototype.destroy.call(this);
		}
	});

//	ontologyLearner.Aspects.initialize = function() {
//		var aspectsList = "#aspectsList";
//		var keywordsList = "#keywordsList";
//		
//		$.getJSON(UrlStore.GetAspectList(), function(aspects) {
//			for (var i=0; i<aspects.length; i++) {
//				var aspect = aspects[i];
//				
//				var option = document.createElement("option");
//				$(option)
//					.attr("value", aspect)
//					.text(aspect)
//					.click(function() {
//						$.getJSON(UrlStore.GetAspect(aspect), function(keywords) {
//							$(keywordsList).empty();
//							
//							for (var j=0; j<keywords.length; j++) {
//								keyword = keywords[j];
//								
//								option = document.createElement("option");
//								$(option)
//									.attr("value", keyword)
//									.text(keyword);
//								$(keywordsList).append(option); 
//							}
//						});
//				});
//				$(aspectsList).append(option);
//			}
//		});
//	};

})(window, window.document, window.jQuery, window.ontologyLearner.UrlStore);