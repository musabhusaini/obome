package controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import models.AspectViewModel;

import org.apache.commons.lang.StringUtils;

import play.Play;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import edu.sabanciuniv.dataMining.experiment.models.Aspect;
import edu.sabanciuniv.dataMining.experiment.models.Keyword;
import edu.sabanciuniv.dataMining.experiment.models.setcover.SetCover;

public class Aspects extends Application {

	public static void list(String collection) {
		SetCover sc = fetch(SetCover.class, collection);
		
		List<AspectViewModel> aspects = Lists.newArrayList();
		for (Aspect aspect : sc.getAspects()) {
			aspects.add(new AspectViewModel(aspect));
		}
		
		Collections.sort(aspects);
		renderJSON(aspects);
	}
	
	public static void downloadableTextFile(String collection) {
		SetCover sc = fetch(SetCover.class, collection);
		
		try {
			File directory = new File(new File(Play.configuration.getProperty("play.tmp", "tmp")),
					Play.configuration.getProperty("sare.downloads", "downloads"));
			
			if (!directory.exists()) {
				directory.mkdir();
			}
			
			File outputFile = File.createTempFile("aspects-" + collection + "-", ".txt", directory);
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			
			for (Aspect aspect : sc.getAspects()) {
				writer.write("<" + aspect.getLabel() + ">\t");
				for (Keyword keyword : aspect.getKeywords()) {
					writer.write(keyword.getLabel() + "\t");
				}
				
				writer.newLine();
			}
			writer.close();
			
			renderBinary(outputFile, "ontology.txt");
		} catch (IOException e) {
			throw new IllegalStateException("Could not write file");
		}
	}
	
	public static void uploadFile(String collection, File file) {
		SetCover sc = fetch(SetCover.class, collection);
		
		try {
			if (file.getName().endsWith(".txt")) {
				Scanner scanner = new Scanner(file);
				
				String lastToken = scanner.next();
				while (scanner.hasNext()) {
					if (!lastToken.endsWith(">")) {
						String rest = scanner.findInLine(".+?>");
						lastToken += " " + rest;
					}
					lastToken = lastToken.replaceAll("[<>]", "");
					
					Aspect aspect = Iterables.getFirst(em().createQuery("SELECT a FROM Aspect a WHERE a.setCover=:sc AND a.label=:label", Aspect.class)
							.setParameter("sc", sc)
							.setParameter("label", lastToken)
							.setMaxResults(1)
							.getResultList(), null);
					if (aspect == null) {
						aspect = new Aspect(sc, lastToken);
						em().persist(aspect);
					}
					
					while (scanner.hasNext() && !(lastToken = scanner.next()).matches("<.+?")) {
						Keyword keyword = Iterables.getFirst(em().createQuery("SELECT k FROM Keyword k WHERE k.aspect=:aspect AND k.label=:label", Keyword.class)
								.setParameter("aspect", aspect)
								.setParameter("label", lastToken)
								.setMaxResults(1)
								.getResultList(), null);
						
						if (keyword == null) {
							keyword = new Keyword(aspect, lastToken);
							em().persist(keyword);
						}
					}
				}
			}
		} catch (RuntimeException | IOException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Could not read file");
		}
		
		renderJSON("{'success': true}");
	}
	
	public static void single(String collection, String aspect) {
		Aspect a = fetch(Aspect.class, aspect);
		renderJSON(new AspectViewModel(a));
	}
	
	public static void postSingle(String collection, String aspect, JsonObject body) {
		AspectViewModel aspectView = new Gson().fromJson(body, AspectViewModel.class);

		// No duplicates.
		SetCover sc = fetch(SetCover.class, collection);
		if (em().createQuery("SELECT a FROM Aspect a WHERE a.setCover=:sc AND a.label=:label", Aspect.class)
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
			
			a = em().merge(a);
		} else {
			if (StringUtils.isEmpty(collection)) {
				throw new IllegalArgumentException("Must provide a collection to add to.");
			}
			
			a = new Aspect(sc, aspectView.label);
			a.setIdentifier(aspectView.uuid);
			
			em().persist(a);
		}

		renderJSON(aspectView);
	}
	
	public static void deleteSingle(String collection, String aspect) {
		Aspect a = fetch(Aspect.class, aspect);
		
		if (StringUtils.isNotEmpty(collection)) {
			if (!a.getSetCover().getIdentifier().toString().equals(collection)) {
				throw new IllegalArgumentException("Aspect being accessed from the wrong collection.");
			}
		}

		em().remove(a);
		
		renderJSON(new AspectViewModel(a));
	}
}