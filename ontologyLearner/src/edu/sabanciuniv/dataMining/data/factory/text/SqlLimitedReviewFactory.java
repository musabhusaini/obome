package edu.sabanciuniv.dataMining.data.factory.text;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqlLimitedReviewFactory extends SqlReviewFactory {

	private int offset;
	private int rowCount;

	public SqlLimitedReviewFactory(int count) {
		this(0, count);
	}

	public SqlLimitedReviewFactory(int offset, int rowCount) {
		super();
		
		this.setOffset(offset);
		this.setRowCount(rowCount);
	}

	public int getOffset() {
		return this.offset;
	}
	
	public void setOffset(int offset) {
		if (offset < 0) {
			throw new IllegalArgumentException("Offset must be positive.");
		}
		
		this.offset = offset;
	}
	
	public int getRowCount() {
		return this.rowCount;
	}
	
	public void setRowCount(int rowCount) {
		if (rowCount < 0) {
			throw new IllegalArgumentException("Row count must be positive.");
		}
		
		this.rowCount = rowCount;
	}
	
	@Override
	protected PreparedStatement prepareStatement() throws SQLException {
		return this.getSqlConnection().prepareStatement("SELECT uuid, content FROM reviews LIMIT " + this.offset + ", " + this.rowCount);
	}
}