package edu.sabanciuniv.dataMining.program;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.collect.ImmutableList;

import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.data.factory.text.SqlRatingBasedIdentifiableObjectFactory;
import edu.sabanciuniv.dataMining.data.factory.ObjectFactory;
import edu.sabanciuniv.dataMining.data.sampling.Category;
import edu.sabanciuniv.dataMining.data.sampling.CategorySet;
import edu.sabanciuniv.dataMining.data.sampling.Sampler;
import edu.sabanciuniv.dataMining.data.writer.SqlDataWriter;

/**
 * @author Mus'ab Husaini
 */
public class DataSamplerProgram {

	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
//		Connection sqlSeverConnection =
//				DriverManager.getConnection("jdbc:sqlserver://localhost;instanceName=SQLEXPRESS;databaseName=TripAdvisor;integratedSecurity=true");
//		Connection mySqlConnection =
//				DriverManager.getConnection("jdbc:mysql://localhost/trip_advisor?user=java_user&password=java_user_pwd");
//		
//		PreparedStatement sqlServerStatement = sqlSeverConnection.prepareStatement("SELECT * FROM reviews");
//		PreparedStatement mySqlStatement =
//				mySqlConnection.prepareStatement("INSERT INTO reviews(hotel_id, author, content, date, rating) VALUES(?, ?, ?, ?, ?)");
//		
//		ResultSet rs = sqlServerStatement.executeQuery();
//		long count = 0;
//		
//		while (rs.next()) {
//			String hotelId = rs.getString("hotel_id");
//			String author = rs.getString("author");
//			String content = rs.getString("content");
//			Date date = rs.getDate("date");
//			Short rating = rs.getShort("rating");
//			
//			mySqlStatement.setString(1, hotelId);
//			mySqlStatement.setString(2, author);
//			mySqlStatement.setString(3, content);
//			mySqlStatement.setDate(4, date);
//			mySqlStatement.setShort(5, rating);
//			mySqlStatement.executeUpdate();
//			
//			System.out.println(++count + ". " + content);
//		}
//		
//		sqlSeverConnection.close();
//		mySqlConnection.close();
		
//		CategorySet categorySet = new CategorySet(ImmutableList.of(new Category("training", 0.75), new Category("testing", 0.25)));
//		SqlDataWriter writer = new SqlDataWriter("reviews"); 
//		for (short i=1; i<=5; i++) {
//			ObjectFactory<IdentifiableObject> factory = new SqlRatingBasedIdentifiableObjectFactory(i);
//			Sampler sampler = new Sampler(factory, categorySet, writer);
//			sampler.sample();
//			factory.close();
//		}
	}
}
