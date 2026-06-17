package ee.joeltek.match_me.dev;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class NameGenerator {
    List<String> firstNames = List.of(
            "Karl", "Martin", "Markus", "Rasmus", "Joonas",
            "Kristjan", "Kaur", "Andreas", "Robin", "Siim",
            "Marten", "Tanel", "Erik", "Kevin", "Sten",
            "Oliver", "Henri", "Sander", "Kaspar", "Mihkel",
            "Rauno", "Argo", "Alar", "Priit", "Veiko",
            "Aivar", "Indrek", "Urmas", "Toomas", "Rein",
            "Jaan", "Peeter", "Ants", "Heiki", "Madis",
            "Silver", "Rainer", "Taavi", "Hannes", "Ivo",
            "Eero", "Lauri", "Ain", "Vallo", "Tiit",
            "Meelis", "Arvo", "Ago", "Kuno", "Oskar"
    );
    List<String> lastNames = List.of(
            "Tamm", "Saar", "Kask", "Mets", "Oja",
            "Kukk", "Rebane", "Ilves", "Põder", "Lepik",
            "Kuusk", "Kalda", "Kivi", "Org", "Rand",
            "Järv", "Sild", "Luik", "Lill", "Pärn",
            "Tammekänd", "Koppel", "Vaher", "Raud", "Sepp",
            "Karu", "Aas", "Nurm", "Uibo", "Kallas",
            "Paju", "Salumets", "Toom", "Roos", "Kangur",
            "Ader", "Teeäär", "Mänd", "Kõiv", "Sarapuu",
            "Lepp", "Kruus", "Veskimäe", "Kivimäe", "Mägi",
            "Ojamaa", "Saare", "Põllu", "Niit", "Salu"
    );
    Random random = new Random(42);
    public String getRandomFirstName() {
        return firstNames.get(random.nextInt(firstNames.size()));
    }
    public String getRandomLastName () {
        return lastNames.get(random.nextInt(lastNames.size()));
    }
}
