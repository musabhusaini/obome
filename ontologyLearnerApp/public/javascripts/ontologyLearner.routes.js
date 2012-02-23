(function(window, $, ontologyLearner) {
	ontologyLearner.routes = ontologyLearner.routes || {};
	
	ontologyLearner.routes = $.extend(ontologyLearner.routes, {
		Application: {
			landingPage: #{jsAction @Application.landingPage() /}
		},
		OpinionCollections: {
			list: #{jsAction @OpinionCollections.list() /},
			single: #{jsAction @OpinionCollections.single(':collection') /},
			browserPage: #{jsAction @OpinionCollections.browserPage(':collection') /},
			Items: {
				list: #{jsAction @OpinionCollections.items(':collection') /},
				seen: #{jsAction @OpinionCollections.seenItems(':collection') /},
				unseen: #{jsAction @OpinionCollections.unseenItems(':collection') /},
				single: #{jsAction @OpinionCollections.singleItem(':collection', ':item') /},
				nextBest: #{jsAction @OpinionCollections.nextBestItem(':collection') /},
			}
		},
		Documents: {
			single: #{jsAction @Documents.single(':document') /}
		},
		Aspects: {
			list: #{jsAction @Aspects.list(':collection') /},
			single: #{jsAction @Aspects.single(':collection', ':aspect') /},
		},
		Keywords: {
			list: #{jsAction @Keywords.list(':collection', ':aspect') /},
			single: #{jsAction @Keywords.single(':collection', ':aspect', ':keyword')/}
		}
	});
})(window, window.jQuery, window.ontologyLearner);