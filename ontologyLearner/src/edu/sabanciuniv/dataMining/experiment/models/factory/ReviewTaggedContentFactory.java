package edu.sabanciuniv.dataMining.experiment.models.factory;

import edu.sabanciuniv.dataMining.data.factory.AbstractObjectFactory;
import edu.sabanciuniv.dataMining.data.factory.ObjectFactory;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.experiment.models.Review;

public class ReviewTaggedContentFactory extends AbstractObjectFactory<TextDocument> {

	private ObjectFactory<Review> reviewFactory;
	private TextDocumentOptions options;

	public ReviewTaggedContentFactory(ObjectFactory<Review> reviewFactory) {
		this(reviewFactory, new TextDocumentOptions());
	}
	
	public ReviewTaggedContentFactory(ObjectFactory<Review> reviewFactory, TextDocumentOptions options) {
		this.setReviewFactory(reviewFactory);
		this.setOptions(options);
	}
	
	public ObjectFactory<Review> getReviewFactory() {
		return reviewFactory;
	}

	public void setReviewFactory(ObjectFactory<Review> reviewFactory) {
		this.reviewFactory = reviewFactory;
	}

	public TextDocumentOptions getOptions() {
		return options;
	}

	public void setOptions(TextDocumentOptions options) {
		this.options = options;
	}

	@Override
	public TextDocument create() {
		super.create();

		Review review = this.reviewFactory.create();
		if (review != null) {
			TextDocument doc = review.getTaggedContent(options);
			System.out.println(this.getCount() + ". " + doc.getSummary());
			return doc;
		}
		return null;
	}

	@Override
	public boolean isPristine() {
		return this.reviewFactory.isPristine();
	}

	@Override
	public boolean reset() {
		if (super.reset()) {
			return this.reviewFactory.reset();
		}
		return false;
	}

	@Override
	public void close() {
		super.close();
		this.reviewFactory.close();
	}
}