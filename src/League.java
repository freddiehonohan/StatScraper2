
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class League extends Thread {

    String name, link, parentDir;
    List<Season> seasons;

    public League(String n, String l) {
        name = n;
        link = l;
        seasons = new ArrayList<>();
        //out.println("League: " + name + " - " + link);
    }

    // This is the entry point for the second thread.
    @Override
    public void run() {
        //out.println("Connecting to - " + link);
        Document doc;
        try {
            doc = Jsoup.connect(link).timeout(Main.timeout).ignoreHttpErrors(true)
                    .followRedirects(true).get();
            // Find out all Seasons
            Elements seasonLinks = doc.select("option"); // direct a after h3
            String seasonYear;
            String seasonLink;
            for (int l = 0; l < seasonLinks.size(); l++) {
                seasonYear = seasonLinks.get(l).text();
                if (!seasonYear.contains("Decimal")
                        && !seasonYear.contains("Fraction")
                        && !seasonYear.contains("American")
                        && !seasonYear.contains("Binary")
                        && !seasonYear.contains("Split")) {
                    seasonLink = link + "/" + seasonYear + "/results";
                    seasons.add(new Season(seasonYear, seasonLink));
                }
            }
            collectSeasons();
        } catch (IOException e) {
        }
    }

    private void collectSeasons() {
        int debugNo = 1;
        if (Main.isDebug) {
            seasons.get(debugNo).start();
            try {
                seasons.get(debugNo).join();
            } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                        System.exit(0);
            }
        } else {
            for (Season season : seasons) {
                try {
                    season.start(); 
                    season.join();
                } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                        System.exit(0);
                }
            }
        }
    }
}
