package edu.sabanciuniv.dataMining.data.factory.text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.sabanciuniv.dataMining.data.factory.AbstractObjectFactory;
import edu.sabanciuniv.dataMining.data.options.HasOptions;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.text.TextDocument;

public class TextFileTweetFactory extends AbstractObjectFactory<TextDocument> implements HasOptions<TextDocumentOptions> {
	private String fileName;
	private BufferedReader reader;
	private TextDocumentOptions options;
	
	private String cleanse(String text) {
		Matcher matcher = Pattern.compile("^\\d+\\\"(.*)\\\"$").matcher(text);
		if (matcher.find()) {
			text = matcher.group(1);
		}
		
		matcher = Pattern.compile("[#@](\\w+)").matcher(text);
		text = matcher.replaceAll("$1");
		
		matcher = Pattern.compile("(?:(?:http://(?:www//.)?)|www//.)\\W+").matcher(text);
		text = matcher.replaceAll("");
		
		return text.trim();
	}
	
	public TextFileTweetFactory(String fileName) {
		this(fileName, new TextDocumentOptions());
	}
	
	public TextFileTweetFactory(String fileName, TextDocumentOptions options) {
		if (fileName == null || fileName.equals("")) {
			throw new IllegalArgumentException("Must provide a valid file name.");
		}
		
		this.fileName = fileName;
		if (!this.reset()) {
			throw new IllegalArgumentException("Must provide a valid file name.");
		}
		
		this.options = options;
	}
		
	@Override
	public TextDocument create() {
		super.create();
		
		String tweet;
		try {
			tweet = this.reader.readLine();
		} catch (IOException e) {
			return null;
		}
		
		if (tweet == null) {
			return null;
		}
		
		if (tweet.equals("")) {
			return this.create();
		}
		
		TextDocument doc = new TextDocument(this.options);
		doc.setText(this.cleanse(tweet));
		
		System.out.println(this.getCount() + ". " + doc.getText());
		System.out.println(doc.getFeatures());
		
		return doc;
	}
	
	@Override
	public void close() {
		super.close();
		
		try {
			this.reader.close();
		} catch (IOException e) {
			// Do nothing.
		}
	}

	@Override
	public boolean reset() {
		super.reset();
		
		if (this.reader != null) {
			this.close();
		}
		
		try {
			this.reader = new BufferedReader(new FileReader(this.fileName));
		} catch (FileNotFoundException e) {
			return false;
		}
		
		return true;
	}

	@Override
	public TextDocumentOptions getOptions() {
		// TODO Auto-generated method stub
		return null;
	}
}