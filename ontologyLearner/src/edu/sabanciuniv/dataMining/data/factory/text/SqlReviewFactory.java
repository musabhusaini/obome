package edu.sabanciuniv.dataMining.data.factory.text;

import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.data.factory.GenericSqlIdentifiableObjectFactory;
import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions;
import edu.sabanciuniv.dataMining.data.options.HasOptions;

/**
 * A factory of reviews based in a SQL database.
 * @author Mus'ab Husaini
 */
public class SqlReviewFactory extends GenericSqlIdentifiableObjectFactory<TextDocument> implements HasOptions<TextDocumentOptions> {
	private TextDocumentOptions options;
	
	public SqlReviewFactory() {
		this(new TextDocumentOptions());
	}
	
	public SqlReviewFactory(TextDocumentOptions options) {
		this.options = options;
	}

	@Override
	protected PreparedStatement prepareStatement() throws SQLException {
		return getSqlConnection().prepareStatement("SELECT uuid, content FROM reviews;");
	}

	@Override
	protected TextDocument createObject(ResultSet sqlRs) throws SQLException {
		TextDocument doc = new TextDocument(this.options);
		
		Object uuidObj = sqlRs.getObject("uuid");
		UUID uuid;
		if (uuidObj instanceof byte[]) {
			ByteBuffer bb = ByteBuffer.wrap((byte[])uuidObj);
			uuid = new UUID(bb.getLong(), bb.getLong());
		} else {
			uuid = UUID.fromString(uuidObj.toString());
		}
		doc.setIdentifier(uuid);
		doc.setText(sqlRs.getString("content"));
		return doc;
	}

	@Override
	protected String objToString(TextDocument obj) {
		return ((TextDocument)obj).getSummary().toString();
	}
	
	@Override
	public TextDocumentOptions getOptions() {
		return this.options;
	}
}