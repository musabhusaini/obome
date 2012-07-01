package controllers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import models.AspectViewModel;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import play.Play;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
			Map<String, List<String>> aspects = Maps.newHashMap();
			
			if (file.getName().endsWith(".txt")) {
				Scanner scanner = new Scanner(file);
				
				String lastToken = scanner.next();
				while (scanner.hasNext()) {
					if (!lastToken.endsWith(">")) {
						String rest = scanner.findInLine(".+?>");
						lastToken += " " + rest;
					}
					lastToken = lastToken.replaceAll("[<>]", "");
					
					List<String> kwList = Lists.newArrayList();
					if (!aspects.containsKey(lastToken)) {
						aspects.put(lastToken, kwList);
					} else {
						kwList = aspects.get(lastToken);
					}
					
					while (scanner.hasNext() && !(lastToken = scanner.next()).matches("<.+?")) {
						if (!kwList.contains(lastToken)) {
							kwList.add(lastToken);
						}
					}
				}
			} else if (file.getName().endsWith(".xml")) {
				XPath xpath = XPathFactory.newInstance().newXPath();
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(new FileInputStream(file));
				doc.getDocumentElement().normalize();

				NodeList aspectNodes = (NodeList)xpath.compile("./aspects/aspect")
					.evaluate(doc, XPathConstants.NODESET);
				for (int index=0; index<aspectNodes.getLength(); index++) {
					Element aspectElem = (Element)aspectNodes.item(index);
					
					String aspectLabel = StringEscapeUtils.unescapeXml((String)xpath.compile("./label[1]/text()")
						.evaluate(aspectElem, XPathConstants.STRING));
					if (StringUtils.isEmpty(aspectLabel)) {
						continue;
					}
					
					List<String> kwList = Lists.newArrayList();
					if (!aspects.containsKey(aspectLabel)) {
						aspects.put(aspectLabel, kwList);
					} else {
						kwList = aspects.get(aspectLabel);
					}
					
					NodeList keywordNodes = (NodeList)xpath.compile("./keywords/keyword/label[1]")
						.evaluate(aspectElem, XPathConstants.NODESET);
					for (int index1=0; index1<keywordNodes.getLength(); index1++) {
						String keywordLabel = StringEscapeUtils.unescapeXml(keywordNodes.item(index1).getTextContent());
						if (StringUtils.isEmpty(keywordLabel)) {
							continue;
						}
						
						if (!kwList.contains(keywordLabel)) {
							kwList.add(keywordLabel);
						}
					}
				}
			} else if (file.getName().endsWith(".json")) {
				JsonArray aspectsArray = new JsonParser().parse(new FileReader(file)).getAsJsonArray();
				
				for (JsonElement aspectElem : aspectsArray) {
					JsonObject aspectObj = aspectElem.getAsJsonObject();
					String aspectLabel = aspectObj.get("label").getAsString();
					if (StringUtils.isEmpty(aspectLabel)) {
						continue;
					}
					
					List<String> kwList = Lists.newArrayList();
					if (!aspects.containsKey(aspectLabel)) {
						aspects.put(aspectLabel, kwList);
					} else {
						kwList = aspects.get(aspectLabel);
					}
					
					JsonArray keywordsArray = aspectObj.get("keywords").getAsJsonArray();
					for (JsonElement keywordElem : keywordsArray) {
						JsonObject keywordObj = keywordElem.getAsJsonObject();
						String keywordLabel = keywordObj.get("label").getAsString();
						if (StringUtils.isEmpty(keywordLabel)) {
							continue;
						}
						
						if (!kwList.contains(keywordLabel)) {
							kwList.add(keywordLabel);
						}
					}
				}
			}
			
			for (Entry<String, List<String>> entry : aspects.entrySet()) {
				Aspect aspect = Iterables.getFirst(em().createQuery("SELECT a FROM Aspect a WHERE a.setCover=:sc AND a.label=:label", Aspect.class)
						.setParameter("sc", sc)
						.setParameter("label", entry.getKey())
						.setMaxResults(1)
						.getResultList(), null);
				if (aspect == null) {
					aspect = new Aspect(sc, entry.getKey());
					em().persist(aspect);
				}
				
				for (String keywordLabel : entry.getValue()) {
					Keyword keyword = Iterables.getFirst(em().createQuery("SELECT k FROM Keyword k WHERE k.aspect=:aspect AND k.label=:label", Keyword.class)
							.setParameter("aspect", aspect)
							.setParameter("label", keywordLabel)
							.setMaxResults(1)
							.getResultList(), null);
					
					if (keyword == null) {
						keyword = new Keyword(aspect, keywordLabel);
						em().persist(keyword);
					}					
				}
			}
			
		} catch (Throwable e) {
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