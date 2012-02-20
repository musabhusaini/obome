package edu.sabanciuniv.dataMining.experiment.models.factory;

import edu.sabanciuniv.dataMining.data.factory.AbstractObjectFactory;
import edu.sabanciuniv.dataMining.data.factory.ObjectFactory;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.experiment.models.OpinionDocument;

public class OpinionDocumentTaggedContentFactory extends AbstractObjectFactory<TextDocument> {

	private ObjectFactory<OpinionDocument> opinionDocumentFactory;
	private TextDocumentOptions options;

	public OpinionDocumentTaggedContentFactory(ObjectFactory<OpinionDocument> opinionFactory) {
		this(opinionFactory, new TextDocumentOptions());
	}
	
	public OpinionDocumentTaggedContentFactory(ObjectFactory<OpinionDocument> opinionDocumentFactory, TextDocumentOptions options) {
		this.setOpinionDocumentFactory(opinionDocumentFactory);
		this.setOptions(options);
	}
	
	public ObjectFactory<OpinionDocument> getOpinionDocumentFactory() {
		return opinionDocumentFactory;
	}

	public void setOpinionDocumentFactory(ObjectFactory<OpinionDocument> opinionDocumentFactory) {
		this.opinionDocumentFactory = opinionDocumentFactory;
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

		OpinionDocument review = this.opinionDocumentFactory.create();
		if (review != null) {
			TextDocument doc = review.getTaggedContent(options);
			System.out.println(this.getCount() + ". " + doc.getSummary());
			return doc;
		}
		return null;
	}

	@Override
	public boolean isPristine() {
		return this.opinionDocumentFactory.isPristine();
	}

	@Override
	public boolean reset() {
		if (super.reset()) {
			return this.opinionDocumentFactory.reset();
		}
		return false;
	}

	@Override
	public void close() {
		super.close();
		this.opinionDocumentFactory.close();
	}
}