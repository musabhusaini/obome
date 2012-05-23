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
			
			if (model.otherInfo.lemma) {
				$(span).attr("lemma", model.otherInfo.lemma);
			}
			
			if (model.otherInfo.aspect) {
				$(span).attr("aspect", model.otherInfo.aspect);
			}
			
			if ($.inArray(displayTypes.standard, model.types) >= 0) {
				$(span).addClass("doc-standard");
			}
			
			if ($.inArray(displayTypes.seen, model.types) >= 0) {
				$(span).addClass("doc-seen");
			}
			
			if ($.inArray(displayTypes.unseen, model.types) >= 0) {
				$(span).addClass("doc-unseen");
			}
			
			if ($.inArray(displayTypes.keyword, model.types) >= 0) {
				$(span).addClass("doc-keyword");
				
				model.otherInfo.aspect &&
					$(span).attr("title", "aspect: " + model.otherInfo.aspect);
			}
			
			if ($.inArray(displayTypes.modifier, model.types) >= 0) {
				$(span).addClass("doc-modifier");
			}

			if ($.inArray(displayTypes.irrelevant, model.types) >= 0) {
				$(span).addClass("doc-irrelevant");
			}

			if ($.inArray(displayTypes.polar, model.types) >= 0) {
				$(span).attr("title", "polarity: " + roundScore(model.otherInfo.polarity));
			}
			
			if ($.inArray(displayTypes.sentencePolarity, model.types) >= 0) {
				$(span)
					.addClass("doc-sentence-polarity")
					.text(" " + getPolarityIndicator(model.otherInfo.polarity))
					.attr("title", "polarity: " + roundScore(model.otherInfo.polarity));
			}
			
			$.each(model.children, function(index, item) {
				$(span).append(reformulateText(item));
			});
			
			if ($.inArray(displayTypes.sentence, model.types) >= 0) {
				$(span).append($("<br>"));
			}
			
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