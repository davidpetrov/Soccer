package scraper;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Scraper {

	public static void main(String[] args) throws IOException {
		Document doc = Jsoup
				.connect("http://int.soccerway.com/national/spain/primera-division/20152016/regular-season/r31781/")
				.get();

		String title = doc.title();
		System.out.println("title: " + title);

		// get all links in page
		Elements links = doc.select("a[href]");
		for (Element link : links) {
			// get the value from the href attribute
			// System.out.println("\nlink: " + link.attr("href"));
			// System.out.println("text: " + link.text());
			if (link.text().equalsIgnoreCase("Matches")) {
				System.out.println("text: " + link.text());
				Document matches = Jsoup.connect(link.attr("abs:href")).get();

				Elements linksM = matches.select("a[href]");
				for (Element linkM : linksM) {
					if (isScore(linkM.text())) {
						System.out.println("text: " + linkM.text());
						System.out.println(link.attr("href"));
						Document fixture = Jsoup.connect(link.attr("abs:href")).get();
						
//						Elements goals = fixtu re.select("h2");
//						for(Element i : goals){
//							System.out.println(i.text());
//						}
//						
						break;
					}
				}

			}
		}

	}

	public static boolean isScore(String text) {
		String[] splitted = text.split("-");

		return splitted.length == 2 && isNumeric(splitted[0].trim()) && isNumeric(splitted[1].trim());
	}

	public static boolean isNumeric(String str) {
		try {
			int d = Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
