package controllers;

public class Pages extends Application {

	public static void show(String page) {
		// TODO: add logic to make sure we get the right page (if needed).
		
		page = page.toLowerCase();
		if (!page.startsWith("@")) {
			page = "@" + page;
		}
		
		renderTemplate(page);
	}
}
