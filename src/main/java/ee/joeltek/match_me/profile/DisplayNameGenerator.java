package ee.joeltek.match_me.profile;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DisplayNameGenerator {
    private static final String[] ADJECTIVES = {
            "Agile","Amber","Ancient","Arctic","Bold","Bright","Calm","Clever","Cosmic","Creative",
            "Curious","Daring","Deep","Eager","Electric","Emerald","Epic","Fearless","Fierce","Focused",
            "Free","Gentle","Golden","Hidden","Infinite","Iron","Keen","Kind","Lively","Lucky",
            "Mighty","Mystic","Nimble","Noble","Radiant","Rapid","Rare","Rising","Silent","Silver",
            "Smart","Solar","Steady","Swift","True","Urban","Vivid","Wild","Wise","Zen",
            "Brave","Chill","Crystal","Dynamic","Elegant","Endless","Frosty","Grand","Humble","Icy",
            "Jolly","Kinetic","Light","Magnetic","Neon","Open","Prime","Quick","Royal","Sharp",
            "Smooth","Solid","Sparkling","Strong","Super","Tender","Ultra","Valiant","Warm","Young"
    };
    private static final String[] NOUNS = {
            "Anchor","Arrow","Atlas","Aurora","Beacon","Blade","Bridge","Comet","Compass","Crown",
            "Crystal","Dragon","Echo","Falcon","Flame","Forest","Galaxy","Harbor","Horizon","Island",
            "Journey","Knight","Lantern","Legend","Lion","Lotus","Matrix","Mountain","Navigator","Nova",
            "Orbit","Owl","Phoenix","Pioneer","Planet","Quest","Ranger","River","Rocket","Shadow",
            "Signal","Sky","Spark","Spirit","Star","Storm","Summit","Tiger","Trail","Voyager",
            "Wave","Wolf","Bridge","Castle","Circle","Cloud","Core","Dawn","Desert","Dream",
            "Engine","Field","Fire","Gate","Glade","Hill","Lake","Leaf","Light","Meadow",
            "Mirror","Ocean","Path","Peak","Riverstone","Road","Root","Shore","Stone","Temple",
            "Tower","Valley","Vision","Wind","Wing","World"
    };
    private final UserProfileRepository profileRepository;

    public String generateUniqueDisplayName () {
        String displayName;
        do {
            int adjIndex = (int)(Math.random() * ADJECTIVES.length);
            int nounIndex = (int)(Math.random() * NOUNS.length);
            int number = 100 + (int)(Math.random() * 899);
            displayName = ADJECTIVES[adjIndex] + NOUNS[nounIndex] + number;
        } while (profileRepository.existsByDisplayNameIgnoreCase(displayName));
        return displayName;
    }
}
