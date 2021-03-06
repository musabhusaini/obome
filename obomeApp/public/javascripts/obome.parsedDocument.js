(function(window, document, $, obome, Utils) {
	
	var displayTypes = {
		root: "ROOT",
		sentence: "SENTENCE",
		standard: "STANDARD",
		separator: "SEPARATOR",
		modifier: "MODIFIER",
		keyword: "KEYWORD",
		polar: "POLAR",
		sentencePolarity: "SENTENCE_POLARITY",
		irrelevant: "IRRELEVANT",
		seen: "SEEN",
		unseen: "UNSEEN"
	};
	
	obome.parsedDocument = function(model, options) {
		
		function roundScore(score) {
			return score.toFixed(options.scorePrecision);
		}
		
		function getPolarityIndicator(polarity) {
			return "[" + (polarity < 0 ? "-" : "+") + "]";
		}
		
		function reformulateText(model) {
			var span = $("<span>").html(model.content || "");
			
			model.otherInfo.lemma &&
				$(span).attr("data-lemma", model.otherInfo.lemma);
			
			model.otherInfo.aspect &&
				$(span).attr("data-aspect", model.otherInfo.aspect);
			
			model.otherInfo.polarity &&
				$(span).attr("data-polarity", model.otherInfo.polarity);

			$.each(model.types, function(index, item) {
				$(span).addClass("doc-" + item.replace("_", "-").toLowerCase());
			});
			
			$.each(model.children, function(index, item) {
				$(span).append(reformulateText(item));
			});
			
			($.inArray(displayTypes.sentence, model.types) >= 0)  &&
				$(span).append($("<br>"));
			
			return span;
		}
		
		if (!(this instanceof obome.parsedDocument)) {
			return new obome.parsedDocument(model);
		}
		
		options = $.extend(true, {}, options, {
			scorePrecision: 3
		});
		
		var me = this;
		var span;
		
		if (typeof model === "object") {
			span = reformulateText(model);
		}
		
		me.getHtml = function() {
			return Utils.outerHtml(span);
		};
		
		me.getJQHtml = function() {
			return span;
		};
		
		return me;
	};
	
})(window, window.document, window.jQuery, window.obome, window.obome.Utils);