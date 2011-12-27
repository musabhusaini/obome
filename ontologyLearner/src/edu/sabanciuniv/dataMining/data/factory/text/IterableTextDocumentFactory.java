package edu.sabanciuniv.dataMining.data.factory.text;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;

import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.data.factory.AbstractObjectFactory;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.options.HasOptions;

public class IterableTextDocumentFactory extends AbstractObjectFactory<TextDocument> implements HasOptions<TextDocumentOptions> {
	Iterable<TextDocument> documentList;
	Iterator<TextDocument> iterator;
	
	public IterableTextDocumentFactory(Iterable<TextDocument> documentList) {
		if (documentList == null) {
			throw new InvalidParameterException("Must provide a list of documents.");
		}
		
		this.documentList = documentList;
		this.iterator = documentList.iterator();
	}
	
	public IterableTextDocumentFactory() {
		this(new ArrayList<TextDocument>());
	}
	
	@Override
	public TextDocument create() {
		super.create();
		if (this.iterator.hasNext()) {
			return this.iterator.next();
		}
		
		return null;
	}

	@Override
	public void close() {
		super.close();
		this.iterator = this.documentList.iterator();
	}

	@Override
	public boolean reset() {
		return super.reset();
	}

	@Override
	public TextDocumentOptions getOptions() {
		return null;
	}
}