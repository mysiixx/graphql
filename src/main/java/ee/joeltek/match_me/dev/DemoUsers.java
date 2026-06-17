package ee.joeltek.match_me.dev;

import java.util.List;

public class DemoUsers {
    public record DemoUser(
            String email,
            String password,
            String firstName,
            String lastName,
            String birthDate,
            String city,
            List<Integer> bioAnswers,
            double longitude,
            double latitude,
            int preferred_radius_meters
    ) {}
    public static final List<DemoUser> DEMO_USERS = List.of(

            // ======== TALLINN – STRONG MATCH GROUP ========

            new DemoUser(
                    "liisa@example.com", "Password123!",
                    "Liisa", "Tamm", "1998-04-12", "Tallinn",
                    List.of(
                            9,9,10,  // Visionary (strong)
                            4,5,4,   // Executor
                            8,8,9,   // Explorer
                            6,5,6,   // Architect
                            8,9,9,   // Harmonizer
                            3,3,2    // Challenger
                    ),
                    24.7536, 59.4370,
                    10000
            ),

            new DemoUser(
                    "mart@example.com", "Password123!",
                    "Mart", "Kask", "1996-08-21", "Tallinn",
                    List.of(
                            9,8,9,
                            5,5,4,
                            9,8,8,
                            5,6,5,
                            8,8,9,
                            3,4,3
                    ),
                    24.7536, 59.4370,
                    10000
            ),

            // ======== TALLINN – CHALLENGER TYPE (WEAK MATCH WITH ABOVE) ========

            new DemoUser(
                    "ragnar@example.com", "Password123!",
                    "Ragnar", "Saar", "1994-02-11", "Tallinn",
                    List.of(
                            4,3,4,
                            8,9,9,
                            5,5,4,
                            6,7,6,
                            3,4,3,
                            10,9,10  // Challenger dominant
                    ),
                    24.7536, 59.4370,
                    10000
            ),

            // ======== TARTU – ARCHITECT + EXECUTOR ========

            new DemoUser(
                    "anna@example.com", "Password123!",
                    "Anna", "Mets", "1997-06-30", "Tartu",
                    List.of(
                            5,5,6,
                            8,9,9,
                            4,5,4,
                            9,9,10,
                            6,7,6,
                            3,3,2
                    ),
                    26.7251, 58.3806,
                    10000
            ),

            new DemoUser(
                    "kristjan@example.com", "Password123!",
                    "Kristjan", "Põld", "1995-01-19", "Tartu",
                    List.of(
                            5,6,5,
                            9,9,8,
                            5,5,4,
                            9,8,9,
                            6,6,7,
                            3,3,2
                    ),
                    26.7251, 58.3806,
                    10000
            ),

            // ======== TARTU – EXPLORER ========

            new DemoUser(
                    "laura@example.com", "Password123!",
                    "Laura", "Rebane", "1999-09-10", "Tartu",
                    List.of(
                            6,6,5,
                            4,5,4,
                            10,9,10,
                            5,5,4,
                            7,8,7,
                            3,3,2
                    ),
                    26.7251, 58.3806,
                    10000
            ),

            // ======== PÄRNU – HARMONIZER ========

            new DemoUser(
                    "kertu@example.com", "Password123!",
                    "Kertu", "Lill", "1998-03-03", "Pärnu",
                    List.of(
                            6,6,5,
                            5,5,4,
                            6,6,7,
                            5,5,5,
                            10,9,10,
                            2,2,2
                    ),
                    24.4971, 58.3859,
                    10000
            ),

            new DemoUser(
                    "marko@example.com", "Password123!",
                    "Marko", "Kivi", "1993-11-22", "Pärnu",
                    List.of(
                            6,5,6,
                            6,5,6,
                            6,7,6,
                            5,5,5,
                            9,10,9,
                            3,3,3
                    ),
                    24.4971, 58.3859,
                    10000
            ),

            // ======== NARVA – CHALLENGER + EXECUTOR ========

            new DemoUser(
                    "alex@example.com", "Password123!",
                    "Alex", "Ivanov", "1992-05-15", "Narva",
                    List.of(
                            4,4,3,
                            9,9,10,
                            4,4,3,
                            7,6,7,
                            3,3,2,
                            10,10,9
                    ),
                    28.1903, 59.3772,
                    10000
            ),

            // ======== TALLINN – BALANCED USERS ========

            new DemoUser(
                    "grete@example.com", "Password123!",
                    "Grete", "Pärn", "2000-07-01", "Tallinn",
                    List.of(
                            7,7,7,
                            6,6,6,
                            7,7,7,
                            6,6,6,
                            7,7,7,
                            6,6,6
                    ),
                    24.7536, 59.4370,
                    10000
            ),

            // ======== RANDOM MIX USERS ========

            new DemoUser(
                    "henry@example.com", "Password123!",
                    "Henry", "Uus", "1991-04-04", "Viljandi",
                    List.of(
                            8,8,7,
                            6,7,6,
                            7,8,7,
                            5,5,6,
                            7,7,6,
                            4,4,3
                    ),
                    25.5977, 58.3639,
                    10000
            ),

            new DemoUser(
                    "marleen@example.com", "Password123!",
                    "Marleen", "Kukk", "1996-12-12", "Rakvere",
                    List.of(
                            7,6,7,
                            6,6,7,
                            6,7,6,
                            6,6,7,
                            8,8,9,
                            3,3,2
                    ),
                    26.3556, 59.3464,
                    10000
            ),

            new DemoUser(
                    "otto@example.com", "Password123!",
                    "Otto", "Sepp", "1994-08-08", "Tallinn",
                    List.of(
                            5,5,5,
                            9,9,9,
                            4,4,4,
                            8,8,8,
                            4,4,4,
                            9,9,10
                    ),
                    24.7536, 59.4370,
                    10000
            ),

            new DemoUser(
                    "eva@example.com", "Password123!",
                    "Eva", "Sild", "1997-10-05", "Tartu",
                    List.of(
                            8,9,8,
                            4,4,5,
                            9,9,8,
                            5,5,5,
                            9,9,10,
                            2,2,2
                    ),
                    26.7251, 58.3806,
                    10000
            ),

            new DemoUser(
                    "raul@example.com", "Password123!",
                    "Raul", "Paju", "1990-06-18", "Pärnu",
                    List.of(
                            4,4,5,
                            8,8,9,
                            5,5,5,
                            7,7,8,
                            4,4,4,
                            9,10,9
                    ),
                    24.4971, 58.3859,
                    10000
            ),

            new DemoUser(
                    "tiina@example.com", "Password123!",
                    "Tiina", "Oja", "1998-01-01", "Tallinn",
                    List.of(
                            9,9,9,
                            3,3,3,
                            8,8,9,
                            5,5,5,
                            9,9,9,
                            2,2,2
                    ),
                    24.7536, 59.4370,
                    10000
            ),

            new DemoUser(
                    "janar@example.com", "Password123!",
                    "Janar", "Mägi", "1995-03-14", "Tartu",
                    List.of(
                            5,5,6,
                            9,8,9,
                            4,5,4,
                            9,9,9,
                            5,6,5,
                            4,4,4
                    ),
                    26.7251, 58.3806,
                    10000
            ),

            new DemoUser(
                    "sandra@example.com", "Password123!",
                    "Sandra", "Kivi", "1999-11-11", "Tallinn",
                    List.of(
                            8,9,8,
                            4,4,5,
                            9,9,9,
                            5,5,6,
                            8,8,9,
                            3,3,2
                    ),
                    24.7536, 59.4370,
                    10000
            ),

            new DemoUser(
                    "denis@example.com", "Password123!",
                    "Denis", "Petrov", "1993-02-02", "Narva",
                    List.of(
                            4,4,4,
                            9,9,9,
                            5,5,5,
                            7,7,7,
                            3,3,3,
                            10,10,10
                    ),
                    28.1903, 59.3772,
                    10000
            ),

            new DemoUser(
                    "katri@example.com", "Password123!",
                    "Katri", "Lepp", "1997-05-25", "Tallinn",
                    List.of(
                            7,8,7,
                            6,6,5,
                            8,9,8,
                            6,6,6,
                            9,9,8,
                            3,3,2
                    ),
                    24.7536, 59.4370,
                    10000
            )
    );

}
