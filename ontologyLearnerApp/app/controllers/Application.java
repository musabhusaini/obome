package controllers;

import java.util.UUID;

import javax.persistence.EntityManager;

import com.google.common.base.Function;

import edu.sabanciuniv.dataMining.data.Identifiable;
import edu.sabanciuniv.dataMining.data.IdentifiableObject;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;
import edu.sabanciuniv.dataMining.program.OntologyLearnerProgram;
import play.cache.Cache;
import play.mvc.Controller;

public class Application extends Controller {

	static class EMFetch<T> implements Function<String,T> {
		private Class<T> clazz;
		
		public EMFetch(Class<T> clazz) {
			this.clazz = clazz;
		}

		@Override
		public T apply(String uuid) {
			EntityManager em = OntologyLearnerProgram.em();
			return em.find(this.clazz, IdentifiableObject.getUuidBytes(UUID.fromString(uuid)));
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
		Boolean bypassCache = params.get("bypassCache", Boolean.class);
		if (bypassCache != null && bypassCache) {
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
			
			encache(obj, idAppendage);
		} else {
			System.out.println("Found " + clazz.getSimpleName() + " from cache.");
			Cache.replace(uuid, obj, "1h");
		}
		
		return obj;
	}
	
	public static <T extends Identifiable> T encache(T obj) {
		return encache(obj, "");
	}
	
	public static <T extends Identifiable> T encache(T obj, String idAppendage) {
		String cacheId = obj.getIdentifier().toString() + idAppendage;
		Cache.set(cacheId, obj, "1h");		
		return obj;
	}
	
	public static <T extends Identifiable> T decache(T obj) {
		return decache(obj, "");
	}
	
	public static <T extends Identifiable> T decache(T obj, String idAppendage) {
		String cacheId = obj.getIdentifier().toString() + idAppendage;
		Cache.delete(cacheId);
		return obj;
	}
	
	public static void index() {
		render();
	}
}