package controllers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
import edu.sabanciuniv.dataMining.experiment.models.Review;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;
import play.mvc.*;

public class Aspects extends Application {

	public static void list(String collection) {
		SetCover sc = fetch(SetCover.class, collection);
		
		List<String> uuids = Lists.newArrayList();
		for (Aspect aspect : sc.getAspects()) {
			uuids.add(aspect.getIdentifier().toString());
		}
		
		renderJSON(uuids);
	}
	
	public static void single(String collection, String aspect) {
		Aspect a = fetch(Aspect.class, aspect);
		renderJSON(new AspectViewModel(a));
	}
	
	public static void postSingle(String collection, String aspect, JsonObject body) {
		AspectViewModel aspectView = new Gson().fromJson(body, AspectViewModel.class);

		Aspect a;
		EntityManager em = OntologyLearnerProgram.em();
		if (aspect.equals(aspectView.uuid)) {
			a = fetch(Aspect.class, aspectView.uuid);
			
			if (StringUtils.isNotEmpty(collection)) {
				if (!a.getSetCover().getIdentifier().toString().equals(collection)) {
					throw new IllegalArgumentException("Aspect being accessed from the wrong collection.");
				}
			}
			
			a.setLabel(aspectView.label);
			
			em.getTransaction().begin();
			a = em.merge(a);
			em.getTransaction().commit();
		} else {
			if (StringUtils.isEmpty(collection)) {
				throw new IllegalArgumentException("Must provide a collection to add to.");
			}
			
			SetCover sc = fetch(SetCover.class, collection);
			a = new Aspect(sc, aspectView.label);
			a.setIdentifier(aspectView.uuid);
			
			em.getTransaction().begin();
			em.persist(a);
			em.getTransaction().commit();
		}

		if (a != null) {
			em.refresh(a);
			decache(a.getSetCover());
			encache(a);
		}

		renderJSON(aspectView);
	}
	
	public static void deleteSingle(String collection, String aspect) {
		EntityManager em = OntologyLearnerProgram.em();
		Aspect a = fetch(Aspect.class, aspect);
		em.refresh(a);
		
		if (StringUtils.isNotEmpty(collection)) {
			if (!a.getSetCover().getIdentifier().toString().equals(collection)) {
				throw new IllegalArgumentException("Aspect being accessed from the wrong collection.");
			}
		}

		em.getTransaction().begin();
		em.remove(a);
		em.getTransaction().commit();
		
		decache(a.getSetCover());
		decache(a);
		
		renderJSON(new AspectViewModel(a));
	}
}