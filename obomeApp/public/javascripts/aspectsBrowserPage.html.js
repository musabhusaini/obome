(function(window, document, $, ontologyLearner, routes) {

	ontologyLearner.displayPage = function(options) {
		var docContainer = "#documentContainer";
		var aspectsContainer = "#aspectsContainer";
		var uploadButton = "#btnUpload";
		var downloadButton = "#btnDownload";
		var nextButton = "#btnNext";
		
		var mainButtonClass = ".main-button";
		
		$(docContainer)
			.documentBrowser($.extend(options, {
				header: "Review Text",
				featureClick: function(event, data) {
					$(aspectsContainer).aspectsBrowser("addKeyword", data);
				}
			}));

		$(aspectsContainer)
			.aspectsBrowser(options);

		$(uploadButton)
			.button();
		
		var uploader = new plupload.Uploader({
			runtimes: "html5,html4",
			browse_button: $(uploadButton).attr("id"),
			container: $(uploadButton).parent().attr("id"),
			max_file_count: 1,
			max_file_size: "10mb",
			chunk_size: "11mb",
			url: routes.Aspects.uploadFile({ collection: options.collection.uuid }),
			filters: [
				{ title : "Text files", extensions : "txt" }
			]
		});

		uploader.init();
		uploader.bind("FilesAdded", function(up, files) {
			$(mainButtonClass).button("disable");
			$(mainButtonClass).spinner();
			up.start();
		});
		uploader.bind("FileUploaded", function(up, file) {
			window.location.reload();
		});
		uploader.bind("Error", function (up, err) {
			$(mainButtonClass).button("enable");
			$(mainButtonClass).spinner("destroy");
			window.alert("Could not upload due to an unknown error");
		});
		
		$(downloadButton)
			.button()
			.click(function() {
				window.open(routes.Aspects.downloadableTextFile({ collection: options.collection.uuid }));
			});
		
		$(nextButton)
			.button()
			.click(function() {
				$(mainButtonClass).button("disable");
				$(mainButtonClass).spinner();
				window.location.href = routes.OpinionCollections.opinionsBrowserPage({ collection: options.collection.uuid });
			});
	};
})(window, window.document, window.jQuery, window.ontologyLearner, window.ontologyLearner.routes)