
import java.util.List;

import static java.lang.System.*;

public class Country extends Thread {

    String name;
    List<League> leagues;

    public Country(String n, List<League> l) {
        name = n;
        leagues = l;
        out.println("Country: " + name + " #Leagues: " + leagues.size());
    }

    @Override
    public void start() {
        if (Main.isDebug) {
            if (!leagues.isEmpty()) {
                leagues.get(0).start();
                try {
                    leagues.get(0).join();
                } catch (InterruptedException e) {
                }
            }
        } else {
            for (League league : leagues) {
                league.start();
            }
            for (League league : leagues) {
                try {
                    league.join();
                } catch (InterruptedException e) {
                    out.println("THREAD ENDED ALREADY");
                }
            }
        }
    }
}