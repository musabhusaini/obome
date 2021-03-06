(function(window, $, obome) {

	obome.routes = $.extend(obome.routes || {}, {
		StaticContent: {
			Images: {
				spinner: "/public/images/spinner.gif"
			}
		},
		Application: {
			landingPage: #{jsAction @Application.landingPage() /},
			Session: {
				ping: #{jsAction @Application.ping() /}
			}
		},
		Pages: {
			show: #{jsAction @Pages.show(':page') /}
		},
		OpinionCollections: {
			list: #{jsAction @OpinionCollections.list() /},
			single: #{jsAction @OpinionCollections.single(':collection') /},
			upload: #{jsAction @OpinionCollections.upload(':corpus') /},
			rename: #{jsAction @OpinionCollections.rename(':corpus') /},
			synthesizer: #{jsAction @OpinionCollections.synthesize(':corpus') /},
			synthesizerPage: #{jsAction @OpinionCollections.synthesizerPage(':corpus') /},
			synthesizerProgress: #{jsAction @OpinionCollections.synthesizerProgress(':corpus') /},
			distillerStats: #{jsAction @OpinionCollections.distillerStats(':collection') /},
			distill: #{jsAction @OpinionCollections.distill(':collection') /},
			distillerProgress: #{jsAction @OpinionCollections.distillerProgress(':collection') /},
			aspectsBrowserPage: #{jsAction @OpinionCollections.aspectsBrowserPage(':collection') /},
			opinionsBrowserPage: #{jsAction @OpinionCollections.opinionsBrowserPage(':collection') /},
			opinionMiner: #{jsAction @OpinionCollections.opinionMiner(':collection', ':document') /},
			Items: {
				list: #{jsAction @OpinionCollections.items(':collection') /},
				seen: #{jsAction @OpinionCollections.seenItems(':collection') /},
				unseen: #{jsAction @OpinionCollections.unseenItems(':collection') /},
				single: #{jsAction @OpinionCollections.singleItem(':collection', ':item') /},
				nextBest: #{jsAction @OpinionCollections.nextBestItem(':collection') /},
			}
		},
		Documents: {
			single: #{jsAction @Documents.single(':collection', ':document') /}
		},
		Aspects: {
			list: #{jsAction @Aspects.list(':collection') /},
			downloadableTextFile: #{jsAction @Aspects.downloadableTextFile(':collection') /},
			uploadFile: #{jsAction @Aspects.uploadFile(':collection') /},
			single: #{jsAction @Aspects.single(':collection', ':aspect') /},
		},
		Keywords: {
			list: #{jsAction @Keywords.list(':collection', ':aspect') /},
			single: #{jsAction @Keywords.single(':collection', ':aspect', ':keyword')/}
		}
	});
})(window, window.jQuery, window.obome);