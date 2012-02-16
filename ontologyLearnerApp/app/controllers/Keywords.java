package controllers;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;

import models.KeywordViewModel;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.sabanciuniv.dataMining.experiment.models.Aspect;
import edu.sabanciuniv.dataMining.experiment.models.Keyword;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;
import play.mvc.*;

public class Keywords extends Application {

	public static void list(String aspect) {
		Aspect a = fetch(Aspect.class, aspect);
		List<KeywordViewModel> keywords = Lists.newArrayList();
		for (Keyword keyword : a.getKeywords()) {
			keywords.add(new KeywordViewModel(keyword));
		}
		
		renderJSON(keywords);
	}
	
	public static void single(String aspect, String keyword) {
		renderJSON(new KeywordViewModel(fetch(Keyword.class, keyword)));
	}
	
	public static void postKeyword(String aspect, String keyword, JsonObject body) {
		KeywordViewModel keywordView = new Gson().fromJson(body, KeywordViewModel.class);
		
		Keyword k;
		EntityManager em = OntologyLearnerProgram.em();
		if (keyword.equals(keywordView.uuid)) {
			k = fetch(Keyword.class, keyword);
			
			if (StringUtils.isNotEmpty(aspect)) {
				if (!k.getAspect().getIdentifier().toString().equals(aspect)) {
					throw new IllegalArgumentException("Keyword being accessed from the wrong aspect.");
				}
			}
			
			k.setLabel(keywordView.label);
			
			em.getTransaction().begin();
			k = em.merge(k);
			em.getTransaction().commit();
		} else {
			if (StringUtils.isEmpty(aspect)) {
				throw new IllegalArgumentException("Must provide a aspect to add to.");
			}

			Aspect a = fetch(Aspect.class, aspect);
			k = new Keyword(a, keywordView.label);
			k.setIdentifier(keywordView.uuid);
			
			em.getTransaction().begin();
			em.persist(k);
			em.getTransaction().commit();
		}
		
		if (k != null) {
			em.refresh(k);
			decache(k.getAspect());
			encache(k);
		}
		
		renderJSON(keywordView);
	}
	
	public static void deleteKeyword(String aspect, String keyword) {
		EntityManager em = OntologyLearnerProgram.em();
		Keyword k = fetch(Keyword.class, keyword);
		em.refresh(k);
		
		if (StringUtils.isNotEmpty(aspect)) {
			if (!k.getAspect().getIdentifier().toString().equals(aspect)) {
				throw new IllegalArgumentException("Keyword being accessed from the wrong aspect.");
			}
		}

		em.getTransaction().begin();
		em.remove(k);
		em.getTransaction().commit();

		decache(k.getAspect());
		decache(k);

		renderJSON(new KeywordViewModel(k));
	}
}
