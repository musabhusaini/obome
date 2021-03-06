(function(window, document, $, obome, routes) {
	var navigationAccordion = "#acrdnNav";
	var accordionOptionUpload = "#acrdnOptUpload";
	var accordionOptionLoad = "#acrdnOptLoad";
	var accordionOptionRetrieve = "#acrdnOptRetrieve";
	var uploaderContainer = "#cntnrUploader";
	var collectionsList = "#lstCollections";
	var keyTextbox = "#txtKey";
	var collectionInfoContainer = "#cntnrCollectionInfo";
	var messageContainer = "#cntnrMessage";
	var nextButton = "#btnNext";

	obome.displayPage = function(options) {
		
		function accordionChange(ui) {
			$(ui)
				.find(":input:first")
				.focus();
		}
		
		$(navigationAccordion).accordion({
			autoHeight: false,
			change: function(event, ui) {
				accordionChange(ui.newContent);
			}
		});
		
		accordionChange(accordionOptionUpload);
		
		$(uploaderContainer)
			.plupload({
				runtimes: "html5,html4",
				url: routes.OpinionCollections.upload({ corpus: "new" }),
				max_file_size: "50mb",
				max_file_count: 20,
				chunk_size: "51mb",
				unique_names : true,
				multiple_queues : true,

				// Rename files by clicking on their titles.
				rename: true,
				
				// Sort files.
				sortable: true,

				// Specify what files to browse for.
				filters: [
					{title: "XML files", extensions: "xml"},
					{title: "Zip files", extensions: "zip"},
					{title: "Text files", extensions: "txt,csv"}
				],
				
				headers: {}
			});
		
		var uploader = $(uploaderContainer).plupload("getUploader");
		uploader.bind("FileUploaded", function(uploader, file, response) {
			response = window.JSON.parse(response.response);
			uploader.settings.url = routes.OpinionCollections.upload({ corpus: response.uuid });
			$(uploaderContainer).data("corpus", response);
		});
		
		$(nextButton)
			.button({
				icons: {
					primary: "ui-icon-circle-arrow-e"
				}
			})
			.click(function() {
				$(nextButton).button("disable");
				$(nextButton).spinner();

				function redirectToSynthesizer(corpus) {
					if (corpus) {
						window.location.href = routes.OpinionCollections.synthesizerPage({ corpus: corpus.uuid });
					}
				}

				var active = $(navigationAccordion).accordion("option", "active");
				if (active === 0) {
					// Upload.
					
			        // Files in queue upload them first.
			        if (uploader.files.length > 0) {
			            // When all files are uploaded submit form.
			            uploader.bind("StateChanged", function() {
			                if (uploader.files.length === (uploader.total.uploaded + uploader.total.failed)) {
			                	redirectToSynthesizer($(uploaderContainer).data("corpus"));
			                }
			            });
			                
			            uploader.start();
			        } else {
			            alert("You must upload at least one file.");
			            $(nextButton).button("enable");
			            $(nextButton).spinner("destroy");
			        }
				} else if (active === 1) {
					// Load.
					var collection = $(collectionsList).find(":selected")
						.data("value");
					
					if (collection.corpusName) {
						window.location.href = routes.OpinionCollections.aspectsBrowserPage({ collection: $(collectionsList).val() });
					} else {
						redirectToSynthesizer(collection);
					}
				} else if (active === 2) {
					// Retrieve.
					$.getJSON(routes.OpinionCollections.single({ collection: $(keyTextbox).val() }))
						.success(function(collection) {
							window.location.href = routes.OpinionCollections.aspectsBrowserPage({ collection: collection.uuid });
						})
						.error(function() {
							$(messageContainer)
								.addClass("ui-state-error")
								.text("Key is not valid")
								.show();
							
							$(nextButton).button("enable");
				            $(nextButton).spinner("destroy");
						});
				}
			});
		
		// Get all collections.
		$(accordionOptionLoad).spinner();
		$.getJSON(routes.OpinionCollections.list())
			.success(function(collections) {
				$(accordionOptionLoad).spinner("destroy");
				
				// Display collections in the list.
				$(collectionsList)
					.empty()
					.change(function(event) {
						var collection = $(event.target).find(":selected")
							.data("value");
						$(collectionInfoContainer)
							.empty()
							.append($("<table>")
								.append("<tr><th>Property</th><th>Value</th></tr>")
								.append("<tr><td>Collection type</td><td>" + (!!collection.corpusName ? "Filtered collection" : "Raw corpus") + "</td></tr>")
								.append(!!collection.corpusName &&
										"<tr><td>Master corpus</td><td>" + collection.corpusName + "</td></tr>")
								.append(!collection.corpusName &&
										"<tr><td>Corpus size</td><td>" + collection.size + "</td></tr>")
								.append(!!collection.corpusName &&
										"<tr><td>Master corpus size</td><td>" + collection.corpusSize + "</td></tr>")
								.append(!!collection.corpusName &&
										"<tr><td>Filtered collection size</td><td>" + collection.size + "</td></tr>")
								.append(!!collection.corpusName &&
										"<tr><td>Error tolerance</td><td>" + collection.errorTolerance + "</td></tr>"));
					});
				$.each(collections, function(index,collection) {
					$(collectionsList)
						.append($("<option>")
							.text(collection.name)
							.val(collection.uuid)
							.data("value", collection));
				});
				
				if ($(collectionsList).find("option").size()) {
					$(collectionsList).change();
				}
			});
	};
})(window, window.document, window.jQuery, window.obome, window.obome.routes)