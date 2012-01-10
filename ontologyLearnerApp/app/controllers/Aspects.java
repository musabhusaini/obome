package controllers;

import org.apache.commons.lang.StringUtils;

import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;
import play.mvc.*;

public class Aspects extends Controller {

	public static void list() {
		OntologyLearnerProgram program = new OntologyLearnerProgram();
		Iterable<String> aspects = program.retrieveExistingAspects();
		
		program.close();
		renderJSON(aspects);
	}

	public static void single(String aspect) {
		OntologyLearnerProgram program = new OntologyLearnerProgram();
		Iterable<String> keywords = program.retrieveKeywords(aspect);
		
		program.close();
		renderJSON(keywords);
	}
	
	public static void postAspect(String aspect) {
		OntologyLearnerProgram program = new OntologyLearnerProgram();
		
		String value = params.get("value");
		boolean success = StringUtils.isNotEmpty(value) ? program.updateAspect(aspect, value) : program.addAspect(aspect);   
		
		program.close();
		if (!success) {
			throw new IllegalStateException("Aspect could not be added or updated, possibly due to a conflict.");
		}
		renderText("Success");
	}
	
	public static void postKeyword(String aspect, String keyword) {
		OntologyLearnerProgram program = new OntologyLearnerProgram();

		String value = params.get("value");
		boolean success = StringUtils.isNotEmpty(value) ? program.updateKeyword(aspect, keyword, value) : program.addKeyword(aspect, keyword); 
		
		program.close();
		if (!success) {
			throw new IllegalStateException("Keyword could not be added or updated, possibly due to a conflict.");
		}
		renderText("Success");
	}
	
	public static void deleteAspect(String aspect) {
		OntologyLearnerProgram program = new OntologyLearnerProgram();
		
		boolean success = program.deleteAspect(aspect);
		
		program.close();
		if (!success) {
			throw new IllegalStateException("Aspect could not be deleted, possibly due to a conflict.");
		}
		renderText("Success");
	}
	
	public static void deleteKeyword(String aspect, String keyword) {
		OntologyLearnerProgram program = new OntologyLearnerProgram();
		
		boolean success = program.deleteKeyword(aspect, keyword);
		
		program.close();
		if (!success) {
			throw new IllegalStateException("Keyword could not be deleted, possibly due to a conflict.");
		}
		renderText("Success");
	}
}