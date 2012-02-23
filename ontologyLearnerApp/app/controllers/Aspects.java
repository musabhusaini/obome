package controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import models.AspectViewModel;
import models.KeywordViewModel;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.sabanciuniv.dataMining.experiment.models.Aspect;
import edu.sabanciuniv.dataMining.experiment.models.Keyword;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;
import play.mvc.*;

public class Aspects extends Application {

	public static void list(String collection) {
		SetCover sc = fetch(SetCover.class, collection);
		
		List<AspectViewModel> aspects = Lists.newArrayList();
		for (Aspect aspect : sc.getAspects()) {
			aspects.add(new AspectViewModel(aspect));
			encache(aspect);
		}
		
		Collections.sort(aspects);
		renderJSON(aspects);
	}
	
	public static void single(String collection, String aspect) {
		Aspect a = fetch(Aspect.class, aspect);
		renderJSON(new AspectViewModel(a));
	}
	
	public static void postSingle(String collection, String aspect, JsonObject body) {
		AspectViewModel aspectView = new Gson().fromJson(body, AspectViewModel.class);

		// No duplicates.
		SetCover sc = fetch(SetCover.class, collection);
		if (em.createQuery("SELECT a FROM Aspect a WHERE a.setCover=:sc AND a.label=:label", Aspect.class)
				.setParameter("sc", sc)
				.setParameter("label", aspectView.label)
				.getResultList().size() > 0) {
			throw new IllegalArgumentException("Aspect already exists");
		}
		
		Aspect a;
		if (aspect.equals(aspectView.uuid)) {
			a = fetch(Aspect.class, aspectView.uuid);
			
			if (StringUtils.isNotEmpty(collection)) {
				if (!a.getSetCover().getIdentifier().toString().equals(collection)) {
					throw new IllegalArgumentException("Aspect being accessed from the wrong collection.");
				}
			}
			
			a.setLabel(aspectView.label);
			
			a = em.merge(a);
		} else {
			if (StringUtils.isEmpty(collection)) {
				throw new IllegalArgumentException("Must provide a collection to add to.");
			}
			
			a = new Aspect(sc, aspectView.label);
			a.setIdentifier(aspectView.uuid);
			
			em.persist(a);
		}

//		if (a != null) {
//			em.refresh(a);
//			decache(a.getSetCover());
//			encache(a);
//		}

		renderJSON(aspectView);
	}
	
	public static void deleteSingle(String collection, String aspect) {
		Aspect a = fetch(Aspect.class, aspect);
		
		if (StringUtils.isNotEmpty(collection)) {
			if (!a.getSetCover().getIdentifier().toString().equals(collection)) {
				throw new IllegalArgumentException("Aspect being accessed from the wrong collection.");
			}
		}

		em.remove(a);
		
		decache(a.getSetCover());
		decache(a);
		
		renderJSON(new AspectViewModel(a));
	}
}