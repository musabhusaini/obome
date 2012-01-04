(function(window, document, $, OntologyLearner, UrlStore, Util) {
	OntologyLearner.Document = OntologyLearner.Document || {};
	
	OntologyLearner.Document.initialize = function() {
		var docContainer = "#documentContainer";
		var nextDocButton = "#nextDoc";
		var prevDocButton = "#prevDoc";
		
		Util.displaySpinner(docContainer);
		$.getJSON(UrlStore.GetDocumentList(), function(uuids) {
			var getDoc = function() {
				Util.displaySpinner(docContainer);
				$.getJSON(UrlStore.GetDocument(uuids[index]), function(doc) {
					$(docContainer).empty();
					$(docContainer).html(window.unescape(doc.text))
				});
			};
		
			var next = function() {
				index++;
				getDoc();
				
				if (index == uuids-1) {
					$(nextDocButton).attr("disabled", true);
				} else {
					$(nextDocButton).removeAttr("disabled");
				}
				
				if (index > 0) {
					$(prevDocButton).removeAttr("disabled");
				}
			};
			
			var prev = function() {
				index--;
				getDoc();
				
				if (index == 0) {
					$(prevDocButton).attr("disabled", "true");
				} else {
					$(prevDocButton).removeAttr("disabled");
				}
				
				$(nextDocButton).removeAttr("disabled");
			};
			
			$(nextDocButton)
				.click(next)
				.attr("disabled", "disabled");
			$(prevDocButton)
				.click(prev)
				.attr("disabled", "disabled");
			
			var index=-1;
			next();
		});
	};
	
})(window, window.document, window.jQuery, window.OntologyLearner, window.OntologyLearner.UrlStore, window.OntologyLearner.Util);