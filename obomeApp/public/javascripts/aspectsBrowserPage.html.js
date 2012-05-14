(function(window, document, $, obome, routes) {

	obome.displayPage = function(options) {
		var docContainer = "#documentContainer";
		var aspectsContainer = "#aspectsContainer";
		var uploadButton = "#btnUpload";
		var downloadButton = "#btnDownload";
		var nextButton = "#btnNext";
		
		var mainButtonSelector = ".ui-main-button";
		var legendButtonSelector = ".legend-button";
		
		$(legendButtonSelector).button();
		
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
			.button({
				icons: {
					primary: "ui-icon-arrowthickstop-1-n"
				}
			});
		
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
			$(mainButtonSelector).button("disable");
			$(mainButtonSelector).spinner();
			up.start();
		});
		uploader.bind("FileUploaded", function(up, file) {
			window.location.reload();
		});
		uploader.bind("Error", function (up, err) {
			$(mainButtonSelector).button("enable");
			$(mainButtonSelector).spinner("destroy");
			window.alert("Could not upload due to an unknown error");
		});
		
		$(downloadButton)
			.button({
				icons: {
					primary: "ui-icon-arrowthickstop-1-s"
				}
			})
			.click(function() {
				window.open(routes.Aspects.downloadableTextFile({ collection: options.collection.uuid }));
			});
		
		$(nextButton)
			.button({
				icons: {
					primary: "ui-icon-circle-arrow-e"
				}
			})
			.click(function() {
				$(mainButtonSelector).button("disable");
				$(mainButtonSelector).spinner();
				window.location.href = routes.OpinionCollections.opinionsBrowserPage({ collection: options.collection.uuid });
			});
	};
})(window, window.document, window.jQuery, window.obome, window.obome.routes)