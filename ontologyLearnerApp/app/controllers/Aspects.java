package controllers;

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
}