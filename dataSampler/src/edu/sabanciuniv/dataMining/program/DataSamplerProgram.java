package edu.sabanciuniv.dataMining.program;

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
		CategorySet categorySet = new CategorySet(ImmutableList.of(new Category("training", 0.75), new Category("testing", 0.25)));
		SqlDataWriter writer = new SqlDataWriter("reviews"); 
		for (short i=1; i<=5; i++) {
			ObjectFactory<IdentifiableObject> factory = new SqlRatingBasedIdentifiableObjectFactory(i);
			Sampler sampler = new Sampler(factory, categorySet, writer);
			sampler.sample();
			factory.close();
		}
	}
}
