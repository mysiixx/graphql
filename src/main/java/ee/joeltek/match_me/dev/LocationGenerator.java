package ee.joeltek.match_me.dev;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class LocationGenerator {
    public record Location(String name, double latitude, double longitude) {}

    List<Location> locations = List.of(
            new Location("Tallinn", 59.4370, 24.7536),
            new Location("Tallinn", 59.455114, 24.689803),
            new Location("Tallinn", 59.414921, 24.657198),
            new Location("Tallinn", 59.433776, 24.831203),
            new Location("Tallinn", 59.444362, 24.884535),
            new Location("Tartu", 58.3776, 26.7290),
            new Location("Tartu", 58.380821, 26.754057),
            new Location("Tartu", 58.355589, 26.720556),
            new Location("Narva", 59.3790, 28.1790),
            new Location("Pärnu", 58.3859, 24.4971),
            new Location("Kohtla-Järve", 59.3986, 27.2731),
            new Location("Viljandi", 58.3639, 25.5972),
            new Location("Rakvere", 59.3464, 26.3553),
            new Location("Maardu", 59.4653, 24.9822),
            new Location("Sillamäe", 59.3969, 27.7633),
            new Location("Kuressaare", 58.2481, 22.5039),
            new Location("Võru", 57.8444, 27.0077),
            new Location("Valga", 57.7778, 26.0473),
            new Location("Haapsalu", 58.9431, 23.5416),
            new Location("Paide", 58.8856, 25.5572),
            new Location("Jõhvi", 59.3569, 27.4211),
            new Location("Keila", 59.3036, 24.4131),
            new Location("Elva", 58.2225, 26.4211),
            new Location("Saue", 59.3226, 24.5497),
            new Location("Rapla", 59.0072, 24.7925),
            new Location("Tapa", 59.2606, 25.9586),
            new Location("Põlva", 58.0603, 27.0696),
            new Location("Jõgeva", 58.7467, 26.3930),
            new Location("Kiviõli", 59.3531, 26.9717),
            new Location("Türi", 58.8086, 25.4327),
            new Location("Paldiski", 59.3561, 24.0531)
    );
    Random random = new Random(42);

    public Location getRandomLocation(){
        Location randomLocation = locations.get(random.nextInt(locations.size()));
        double distanceKm = 2 + random.nextDouble() * 4; // 2–6 km
        double angle = random.nextDouble() * 2 * Math.PI;

        double deltaLat = distanceKm / 111.0;
        double deltaLon = distanceKm / (111.0 * Math.cos(Math.toRadians(randomLocation.latitude())));

        double latitude = randomLocation.latitude() + deltaLat * Math.cos(angle);
        double longitude = randomLocation.longitude() + deltaLon * Math.sin(angle);

        return new Location(randomLocation.name(), latitude, longitude);
    }

}
