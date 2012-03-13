package controllers;

import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.log4j.lf5.util.DateFormatManager;

import models.SessionViewModel;
import play.Logger;
import play.cache.Cache;
import play.classloading.enhancers.EnhancedForContinuations;
import play.libs.Codec;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Controller;
import play.mvc.Finally;
import play.mvc.Http.Request;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import edu.sabanciuniv.dataMining.data.Identifiable;
import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;

public class Application extends Controller implements EnhancedForContinuations {

	protected static String constructGenericLogMessage(String message) {
		DateFormatManager dfm = new DateFormatManager(TimeZone.getDefault());
		
		return String.format("Message=%s while executing request-type=%s method=%s action=%s made-on=%s through-url=%s in-session=%s from-client=%s",
				message,
				request.isAjax() ? "ajax" : "browser",
				request.method,
				request.action,
				dfm.format(request.date),
				request.url,
				session.getId(),
				request.remoteAddress);
	}
	
	protected static EntityManager em() {
		EntityManager em = entityManagers.get(request);
		if (em == null) {
			em = OntologyLearnerProgram.em();
			entityManagers.put(request, em);
		}
		
		return em;
	}
	
	protected static Map<Request, EntityManager> entityManagers;

	static {
		entityManagers = Maps.newHashMap();
	}
	
	@Before
	static void logActionBegin() {
		Logger.info(constructGenericLogMessage("began"));
	}
	
	@Before
	static void initializeEntityManager() {
		if (!em().getTransaction().isActive()) {
			em().getTransaction().begin();
		}
	}
		
	@After
	static void rekindleSession() {
		SessionViewModel sessionViewModel = SessionViewModel.findById(session.getId());
		
		if (sessionViewModel == null) {
			sessionViewModel = new SessionViewModel();
			sessionViewModel.setIdentifier(session.getId()); 
		}
		
		sessionViewModel.keepAlive();
		
		Logger.info("Session %s being kept alive", session.getId());
	}
	
	@After
	static void finalizeEntityManager() {
		if (em().getTransaction().isActive()) {
			em().getTransaction().commit();
		}
	}
	
	@After
	static void logActionEnd() {
		Logger.info(constructGenericLogMessage("finished"));
	}
	
	@Catch(Throwable.class)
	static void logGenericException(Throwable throwable) {
		Logger.error(throwable, constructGenericLogMessage("error"));
		
		if (em().getTransaction().isActive()) {
			em().getTransaction().rollback();
		}
	}
	
	@Finally
	static void closeEntityManager() {
		if (em().isOpen()) {
			em().close();
		}
		if (entityManagers.containsKey(request)) {
			entityManagers.remove(request);
		}
	}
	
	static class EMFetch<T> implements Function<String,T> {
		private Class<T> clazz;
		
		public EMFetch(Class<T> clazz) {
			this.clazz = clazz;
		}

		@Override
		public T apply(String uuid) {
			T obj = em().find(this.clazz, IdentifiableObject.getUuidBytes(IdentifiableObject.createUuid(uuid)));
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
//			System.out.println("Bypassing cache as requested.");
			
			return fallback.apply(uuid);
		}
		
		String cacheId = uuid + idAppendage;
		T obj = Cache.get(cacheId, clazz);
		if (obj == null) {
//			System.out.println("Couldn't find " + clazz.getSimpleName() + " in cache, looking up in DB.");
			
			obj = fallback.apply(uuid);
			
			if (obj == null) {
				throw new IllegalArgumentException("No such item exists.");
			}
		} else {
//			System.out.println("Found " + clazz.getSimpleName() + " from cache.");
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