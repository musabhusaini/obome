package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.sabanciuniv.dataMining.data.options.text.TextDocumentOptions.FeatureType;
import edu.sabanciuniv.dataMining.data.text.TextDocument;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;

public class Application extends Controller {

    public static void index() {    	
        render();
    }
}