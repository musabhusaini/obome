(function(window, document, $) {
	
	$.widget("widgets.editableListbox", {
		options: {
			items: [],
			multiselect: false,
			size: 0,
			selected: []
		},
		
		_listbox: null,
		
		refresh: function() {
			var me = this;
			var list = me.options.items = me.options.items || [];
			
			$(me._listbox).empty();
			
			if (me.options.size === 0) {
				$(me._listbox).addClass("ui-fit-vertical");
			}
			
			var listItem = null;
			var tmpList = me.options.items;
			me.options.items = [];
			while ((listItem = tmpList.shift())) {
				me.addItem(listItem.item || listItem, listItem.value || listItem.value || listItem);
			}
		},
	
		addItem: function(item, value) {
			var me = this;
			var listbox = me._listbox;
			
			if (!value) {
				value = item;
			}
			
			var data = {
				item: item,
				value: value,
				index: me.options.items.length
			};
			me.options.items.push(data);
			
			var listItem = $(document.createElement("li"))
				.addClass("ui-list-item")
				.text(item)
				.click(function(event) {
					if (!event.ctrlKey || !me.options.multiselect) {
						$(me._listbox)
							.find("li")
							.removeClass("ui-state-highlight");
						
						me.options.selected = [];
					}
					
					$(event.target).addClass("ui-state-highlight");
					me.options.selected.push($(event.target).data("itemData"));
				})
				.dblclick(function(event) {
					
				})
				.keypress(function(event) {
					
				})
				.data("itemData", data);
			
			$(listbox).append(listItem);
		},
		
		_create: function() {
			var me = this;
			var listbox = me._listbox = $(document.createElement("ul"))
				.addClass("ui-widget")
				.addClass("ui-widget-content")
				.addClass("ui-editable-list")
				.insertAfter(me.element);
			
			if ($(me.element).is("select")) {
				me.options.items = [];
				$(me.element).find("option").each(function(i, elem) {
					elem = $(elem);
					me.options.items.push({
						item: $(elem).text(),
						value: $(elem).val()
					});
				});
			}
			$(me.element).hide();
			
			me.refresh();
			$(listbox).hide();
		},
		
		_init: function() {
			$(this._listbox).show();
		},
		
		_setOption: function(key, value) {
			$.Widget.prototype._setOption.apply(this, arguments);
		},
		
		destroy: function() {
			$.Widget.prototype.destroy.call(this);
		}
	});

})(window, window.document, window.jQuery);