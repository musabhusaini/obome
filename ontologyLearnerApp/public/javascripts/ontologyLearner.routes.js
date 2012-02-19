(function(window, $, ontologyLearner) {
	ontologyLearner.routes = ontologyLearner.routes || {};
	
	ontologyLearner.routes = $.extend(ontologyLearner.routes, {
		Application: {
			index: #{jsAction @Application.index() /}
		},
		ReviewCollections: {
			list: #{jsAction @ReviewCollections.list() /},
			single: #{jsAction @ReviewCollections.single(':collection') /},
			Items: {
				list: #{jsAction @ReviewCollections.items(':collection') /},
				seen: #{jsAction @ReviewCollections.seenItems(':collection') /},
				unseen: #{jsAction @ReviewCollections.unseenItems(':collection') /},
				single: #{jsAction @ReviewCollections.singleItem(':collection', ':item') /},
				nextBest: #{jsAction @ReviewCollections.nextBestItem(':collection') /},
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