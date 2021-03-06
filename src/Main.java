
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

    public static int timeout = 10000;
    //public static boolean isDebug = false;
    public static boolean isDebug = true;
    static String countryTags[] = {"England", "Scotland", "International",
        "Europe", "Spain", "Italy", "Germany", "France", "Holland",
        "Belgium", "Portugal", "Greece", "Turkey", "Austria",
        "Switzerland", "Denmark", "Finland", "Norway", "Sweden", "Wales",
        "Northern Ireland", "Republic of Ireland"};
    static int minMatchesPlayed = 6;

    public static void main(String[] args) throws IOException {
        File file = new File("trainingData.txt");
    		if(file.delete()){
    			System.out.println(file.getName() + " is deleted!");
    		}else{
    			System.out.println("Delete operation is failed.");
    		}
        
        
        long start = System.currentTimeMillis();
// do your work...
        // code
        String url = "http://www.statto.com";
        out.println("Connecting to - " + url);
        Document doc = Jsoup.connect(url).timeout(timeout).ignoreHttpErrors(true)
                .followRedirects(true).get();
        Elements links = doc.select("a[href]");

        String countryTag;
        Element link;
        List<Country> countries = new ArrayList<>();
        countryLoop:
        for (int countryIndex = 0; countryIndex < countryTags.length; countryIndex++) {
            countryTag = countryTags[countryIndex];
            if (countryIndex == 2 || countryIndex == 3)// Skip International &
            // European Cups
            {
                continue countryLoop;
            }
            for (int linkIndex = 0; linkIndex < links.size(); linkIndex++) {
                link = links.get(linkIndex);
                if (link.text().contains(countryTag)) {
                    // out.println("--------------------------------\nCountry: "
                    // + link.text());
                    linkIndex++;
                    link = links.get(linkIndex);
                    List<League> leagues = new ArrayList<>();
                    boolean foundAllLeagues = false;
                    while (!foundAllLeagues) {
                        // Ireland only has one league
                        if (countryTag.contains("Republic of Ireland")) {
                            leagues.add(new League(link.text(), link
                                    .attr("abs:href")));
                            foundAllLeagues = true;
                        } // Or else we've gotten to the next country
                        else if (link.text().contains(
                                countryTags[countryIndex + 1])) {
                            foundAllLeagues = true;
                        } // Otherwise there are more Leagues
                        else {
                            if (link.text().contains("Play-Offs")
                                    || link.text().contains("Table")
                                    || link.text().contains("P-O 1")
                                    || link.text().contains("Qual.") || !link.attr("abs:href").contains("statto.com")) {
                                foundAllLeagues = true;
                            } else {
                                leagues.add(new League(link.text(), link
                                        .attr("abs:href")));
                                linkIndex++;
                                if (linkIndex < links.size()) {
                                    link = links.get(linkIndex);
                                } else {
                                    foundAllLeagues = true;
                                }
                            }
                        }
                    }// while
                    countries.add(new Country(countryTag, leagues));
                }// if
            }// for

        }// for
        if(isDebug){
        countries.get(6).start();
            try {
                countries.get(6).join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println(ex.getMessage());
                        System.exit(0);
            }
        }
        else{
        for(Country c : countries){
            //c.start();
        }
        for(Country c : countries){
            try {
                c.start();
                c.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println(ex.getMessage());
                        System.exit(0);
            }
        }
        }
        int totalCountries = 0;
        int totalLeagues = 0;
        int totalSeasons = 0;
        int totalMatchDays = 0;
        int totalMatches = 0;
        for (Country c : countries) {
            for (League l : c.leagues) {
                for (Season s : l.seasons) {
                    for (Month m : s.months) {
                        for (Day d : m.days) {
                            for (Match ma : d.matches) {
                                totalMatches++;
                            }
                            totalMatchDays++;
                        }
                    }
                    totalSeasons++;
                }
                totalLeagues++;
            }
            totalCountries++;
        }
        out.println("Total Countries:" + totalCountries);
        out.println("Total Leagues:" + totalLeagues);
        out.println("Total Seasons:" + totalSeasons);
        out.println("Total MatchDays:" + totalMatchDays);
        out.println("Total Matches:" + totalMatches);
        PredictionNetwork p = new PredictionNetwork();
        //p.start();
        long elapsed = System.currentTimeMillis() - start;
        System.out.println(getDurationBreakdown(elapsed));
        
        
    }
    /**
     * Convert a millisecond duration to a string format
     * 
     * @param millis A duration to convert to a string form
     * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
     */
    public static String getDurationBreakdown(long millis)
    {
        if(millis < 0)
        {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(days);
        sb.append(" Days ");
        sb.append(hours);
        sb.append(" Hours ");
        sb.append(minutes);
        sb.append(" Minutes ");
        sb.append(seconds);
        sb.append(" Seconds");

        return(sb.toString());
    }
}
