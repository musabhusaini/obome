(function(window, document, $, OntologyLearner, UrlStore, Util) {
	OntologyLearner.Document = OntologyLearner.Document || {};

	var widget = null;
	
	var headerClass = "header";
	var textContainerClass = "textContainer";
	var nextButtonClass = "nextButton";
	var prevButtonClass = "prevButton";
	var navButtonsClass = "navButton";

	// Just a helper to make things easier.
	var $$ = function(domClass) {
		return "." + domClass;
	};
	
	widget = {
		options: {
			header: "Review:",
			uuids: null,
			offset: 0,
		},
		
		
		refresh: function() {
			var me = this;
			var uuids = me.options.uuids;
			var index = me.options.offset;
			
			if (index < 0 || index >= uuids.length) {
				return;
			}

			Util.displaySpinner($(me.element).find($$(textContainerClass)));
			$.getJSON(UrlStore.GetDocument(uuids[index]), function(doc) {
				$(me.element)
					.find($$(textContainerClass))
					.empty();
				$(me.element)
					.find($$(textContainerClass))
					.html(window.unescape(doc.text))
			});

			$(me.element).find($$(prevButtonClass)).button("option", {
				disabled: (index === 0)
			});

			$(me.element).find($$(nextButtonClass)).button("option", {
				disabled: (index === uuids.length-1)
			});
		},
	
		_create: function() {
			var me = this;
			
			var goForward = function() {
				me.option({ offset: me.options.offset+1 });
			};
			
			var goBack = function() {
				me.option({ offset: me.options.offset-1 });
			};
			
			var headerElem = $(document.createElement("h3"))
				.addClass(headerClass)
				.text(me.options.header);
			var textContainerElem = $(document.createElement("div"))
				.addClass(textContainerClass)
				.addClass("document")
				.text("This is a placeholder for a review")
			var prevButtonElem = $(document.createElement("div"))
				.addClass(prevButtonClass)
				.addClass(navButtonClass)
				.button({
					disabled: true,
					text: false,
					icons: {
						primary: "ui-icon-circle-triangle-w"
					}
				})
				.click(goBack);
			var nextButtonElem = $(document.createElement("div"))
				.addClass(nextButtonClass)
				.addClass(navButtonClass)
				.button({
					disabled: true,
					text: false,
					icons: {
						primary: "ui-icon-circle-triangle-e"
					}
				})
				.click(goForward);
			
			$($$(navButtonsClass)).buttonset();
			
			$(me.element)
				.append(headerElem)
				.append(textContainerElem)
				.append(prevButtonElem)
				.append(nextButtonElem);
			
			$(nextButtonElem).position({
				my: "left",
				at: "right",
				of: $(prevButtonElem),
				offset: "10 0"
			});

			if (!me.options.uuids) {
				Util.displaySpinner(textContainerElem);
				$.getJSON(UrlStore.GetDocumentList(), function(uuids) {
					me.option({
						uuids: uuids,
						offset: 0
					});
				});
			} else {
				me.refresh();
			}
		},
		
		_setOption: function(key, value) {
			$.Widget.prototype._setOption.apply(this, arguments);
			
			// TODO: put stuff here.
			if (key === "header") {
				$(me.element).find($$(headerClass)).text(value);
			} else if (key === "uuids" || key === "offset") {
				this.refresh();
			}
		},
		
		destroy: function() {
			$(this.element).empty();
			$.Widget.prototype.destroy.call(this);
		}
	};
	
	$.widget("ontologyLearner.documentWidget", widget);
	
})(window, window.document, window.jQuery, window.OntologyLearner, window.OntologyLearner.UrlStore, window.OntologyLearner.Util);