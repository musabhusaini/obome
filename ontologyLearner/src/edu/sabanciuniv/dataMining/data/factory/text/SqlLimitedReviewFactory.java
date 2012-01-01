package edu.sabanciuniv.dataMining.data.factory.text;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqlLimitedReviewFactory extends SqlReviewFactory {

	private int lowerLimit;
	private int higherLimit;

	public SqlLimitedReviewFactory(int higherLimit) {
		this(0, higherLimit);
	}

	public SqlLimitedReviewFactory(int lowerLimit, int higherLimit) {
		super();
		
		this.setLowerLimit(lowerLimit);
		this.setHigherLimit(higherLimit);
	}

	public int getLowerLimit() {
		return this.lowerLimit;
	}
	
	public void setLowerLimit(int lowerLimit) {
		if (lowerLimit > this.higherLimit || lowerLimit < 0) {
			throw new IllegalArgumentException("Lower limit must be less than the higher limit and >= 0.");
		}
		
		this.lowerLimit = lowerLimit;
	}
	
	public int getHigherLimit() {
		return this.higherLimit;
	}
	
	public void setHigherLimit(int higherLimit) {
		if (higherLimit < this.lowerLimit) {
			throw new IllegalArgumentException("Higher limit must be greater than or equal to the lower limit.");
		}
		
		this.higherLimit = higherLimit;
	}
	
	@Override
	protected PreparedStatement prepareStatement() throws SQLException {
		String sql = "SELECT uuid, content FROM reviews ";
		String add = "";
		
		if (this.lowerLimit != 0) {
			add += "LIMIT " + this.lowerLimit;
		}
		
		if (this.higherLimit != 0) {
			if (!add.equals("")) {
				add += ", " + this.higherLimit;
			} else {
				add += "LIMIT " + this.higherLimit;
			}
		}
		
		return this.getSqlConnection().prepareStatement((sql + add).trim());
	}
}