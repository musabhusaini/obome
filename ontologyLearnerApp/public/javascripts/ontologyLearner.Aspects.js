(function(window, document, $, ontologyLearner, UrlStore, Util) {
	ontologyLearner.Aspects = ontologyLearner.Aspects || {};
	
	ontologyLearner.Aspects.initialize = function() {
		var aspectsList = "#aspectsList";
		var keywordsList = "#keywordsList";
		
		$.getJSON(UrlStore.GetAspectList(), function(aspects) {
			for (var i=0; i<aspects.length; i++) {
				var aspect = aspects[i];
				
				var option = document.createElement("option");
				$(option)
					.attr("value", aspect)
					.text(aspect)
					.click(function() {
						$.getJSON(UrlStore.GetAspect(aspect), function(keywords) {
							$(keywordsList).empty();
							
							for (var j=0; j<keywords.length; j++) {
								keyword = keywords[j];
								
								option = document.createElement("option");
								$(option)
									.attr("value", keyword)
									.text(keyword);
								$(keywordsList).append(option); 
							}
						});
				});
				$(aspectsList).append(option);
			}
		});
	};
})(window, window.document, window.jQuery, window.ontologyLearner, window.ontologyLearner.UrlStore, window.ontologyLearner.Util);