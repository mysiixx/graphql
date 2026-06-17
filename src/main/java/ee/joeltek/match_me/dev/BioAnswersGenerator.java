package ee.joeltek.match_me.dev;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class BioAnswersGenerator {
    Random random = new Random(42);
     public List<Integer> getRandomBioAnswers () {
         List <Integer> answers = new ArrayList<>();

         // 0=Visionary, 1=Executor, 2=Explorer, 3=Architect, 4=Harmonizer, 5=Challenger
         int dominant = random.nextInt(6);

         for (int i = 0; i < 18; i++) {
             int archetypeIndex = i / 3; // every 3 answers same archetype
             answers.add(generateScore(archetypeIndex, dominant, random));
         }


         return answers;
     }

    int generateScore(int archetypeIndex, int dominant, Random random) {
        //to position archetypes more psychologically on ring
         Map<Integer, Integer> ringIndex = Map.of(
                0, 0, // Visionary
                2, 1, // Explorer
                5, 2, // Challenger
                1, 3, // Executor
                3, 4, // Architect
                4, 5  // Harmonizer
        );

        int r1 = ringIndex.get(archetypeIndex);
        int r2 = ringIndex.get(dominant);

        int distance = Math.min(
                Math.abs(r1 - r2),
                6 - Math.abs(r1 - r2)
        );

        if (distance == 0) {
            return 7 + random.nextInt(4); // 7–10 (dominant)
        } else if (distance == 1) {
            return 5 + random.nextInt(3); // 5–7 (close)
        } else if (distance == 2) {
            return 3 + random.nextInt(3); // 3–5
        } else {
            return 1 + random.nextInt(3); // 1–3 (opposite)
        }
    }




}
