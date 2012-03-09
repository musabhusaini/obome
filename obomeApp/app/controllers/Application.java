package controllers;

import java.util.UUID;

import javax.persistence.EntityManager;

import models.SessionViewModel;
import play.cache.Cache;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;

import com.google.common.base.Function;

import edu.sabanciuniv.dataMining.data.Identifiable;
import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;

public class Application extends Controller {

	protected static EntityManager em;

	@Before
	static void initializeEntityManager() {
		em = OntologyLearnerProgram.em();
		em.getTransaction().begin();
	}
	
	@After
	static void finalizeEntityManager() {
		if (em.getTransaction().isActive()) {
			em.getTransaction().commit();
		}
		em.close();
	}
	
	@After
	static void rekindleSession() {
		SessionViewModel sessionViewModel = SessionViewModel.findById(session.getId());
		
		if (sessionViewModel == null) {
			sessionViewModel = new SessionViewModel();
			sessionViewModel.setIdentifier(session.getId()); 
		}
		
		sessionViewModel.keepAlive();
	}
	
	static class EMFetch<T> implements Function<String,T> {
		private Class<T> clazz;
		
		public EMFetch(Class<T> clazz) {
			this.clazz = clazz;
		}

		@Override
		public T apply(String uuid) {
			T obj = em.find(this.clazz, IdentifiableObject.getUuidBytes(IdentifiableObject.createUuid(uuid)));
			return obj;
		}
	}

	public static <T extends Identifiable> T fetch(Class<T> clazz, String uuid) {
		return fetch(clazz, uuid, "");
	}
	
	public static <T extends Identifiable> T fetch(Class<T> clazz, String uuid, String idAppendage) {
		return fetch(clazz, uuid, idAppendage, new EMFetch<T>(clazz));
	}
	
	public static <T extends Identifiable> T fetch(Class<T> clazz, String uuid, Function<String,T> fallback) {
		return fetch(clazz, uuid, "", fallback);
	}
	
	public static <T extends Identifiable> T fetch(Class<T> clazz, String uuid, String idAppendage, Function<String,T> fallback) {
		Boolean bypassCache = true; //params.get("bypassCache", Boolean.class);
		if (bypassCache != null && bypassCache) {
			System.out.println("Bypassing cache as requested.");
			
			return fallback.apply(uuid);
		}
		
		String cacheId = uuid + idAppendage;
		T obj = Cache.get(cacheId, clazz);
		if (obj == null) {
			System.out.println("Couldn't find " + clazz.getSimpleName() + " in cache, looking up in DB.");
			
			obj = fallback.apply(uuid);
			
			if (obj == null) {
				throw new IllegalArgumentException("No such item exists.");
			}
		} else {
			System.out.println("Found " + clazz.getSimpleName() + " from cache.");
		}
		
		encache(obj, idAppendage);
		return obj;
	}
	
	public static <T extends Identifiable> T encache(T obj) {
		return encache(obj, "");
	}
	
	public static <T extends Identifiable> T encache(T obj, String idAppendage) {
//		String cacheId = obj.getIdentifier().toString() + idAppendage;
//		Cache.set(cacheId, obj, "1h");
//		
//		System.out.println("Storing a " + obj.getClass().getSimpleName() + " in cache as " + cacheId);
		return obj;
	}
	
	public static <T extends Identifiable> T decache(T obj) {
		return decache(obj, "");
	}
	
	public static <T extends Identifiable> T decache(T obj, String idAppendage) {
//		String cacheId = obj.getIdentifier().toString() + idAppendage;
//		Cache.delete(cacheId);
//		
//		System.out.println("Deleting a " + obj.getClass().getSimpleName() + " from cache as " + cacheId);
		return obj;
	}
	
	public static void landingPage() {
		render();
	}
	
	public static void ping() {
		renderText("Good deal!");
	}
	
	public static void testPage() {
		render();
	}
	
	public static void aboutPage() {
		render();
	}
}