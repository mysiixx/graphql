package ee.joeltek.match_me.bio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BioService {
    private final BioRepository bioRepository;

    public Archetype updateBioScores(UserBio bio, List<Integer> answers) {
        if (answers == null || answers.size() != 18) {
            throw new IllegalArgumentException("Must provide exactly 18 answers");
        }

        List<ArchetypeResult> results = new ArrayList<>();

        results.add(calculateResult(Archetype.VISIONARY, answers.subList(0, 3)));
        results.add(calculateResult(Archetype.EXECUTOR, answers.subList(3, 6)));
        results.add(calculateResult(Archetype.EXPLORER, answers.subList(6, 9)));
        results.add(calculateResult(Archetype.ARCHITECT, answers.subList(9, 12)));
        results.add(calculateResult(Archetype.HARMONIZER, answers.subList(12, 15)));
        results.add(calculateResult(Archetype.CHALLENGER, answers.subList(15, 18)));

        results.sort(
                Comparator.comparingInt(ArchetypeResult::score).reversed()
                        .thenComparingInt(ArchetypeResult::variance)
                        .thenComparingInt(r -> r.archetype().ordinal())
        );

        Archetype primary = results.getFirst().archetype();

        for (ArchetypeResult res : results) {
            switch (res.archetype()) {
                case VISIONARY -> bio.setVisionaryScore(res.score());
                case CHALLENGER -> bio.setChallengerScore(res.score());
                case ARCHITECT -> bio.setArchitectScore(res.score());
                case HARMONIZER -> bio.setHarmonizerScore(res.score());
                case EXPLORER -> bio.setExplorerScore(res.score());
                case EXECUTOR -> bio.setExecutorScore(res.score());
            }
        }

        return primary;
    }

    public double calculateMatchPercentage(UserBio userA, UserBio userB) {
        int distance = 0;

        // Pair 1: Visionary & Executor (Ideas vs. Action)
        distance += Math.abs(userA.getVisionaryScore() - userB.getExecutorScore());
        distance += Math.abs(userA.getExecutorScore() - userB.getVisionaryScore());

        // Pair 2: Explorer & Architect (Spontaneity vs. Structure)
        distance += Math.abs(userA.getExplorerScore() - userB.getArchitectScore());
        distance += Math.abs(userA.getArchitectScore() - userB.getExplorerScore());

        // Pair 3: Harmonizer & Challenger (Empathy vs. Logic)
        distance += Math.abs(userA.getHarmonizerScore() - userB.getChallengerScore());
        distance += Math.abs(userA.getChallengerScore() - userB.getHarmonizerScore());

        // Calculate match fraction based on max possible distance (162)
        double matchFraction = 1.0 - ((double) distance / 162.0);
        double rawPercentage = matchFraction * 100.0;

        // Round to 1 decimal place and safeguard against negative numbers
        return Math.max(0.0, Math.round(rawPercentage * 10.0) / 10.0);
    }

    private ArchetypeResult calculateResult(Archetype archetype, List<Integer> answers) {
        int score = 0;
        int min = 10;
        int max = 1;

        for (int ans : answers) {
            score += ans;
            if (ans < min) min = ans;
            if (ans > max) max = ans;
        }

        int variance = max - min;
        return new ArchetypeResult(archetype, score, variance);
    }

    private record ArchetypeResult(Archetype archetype, int score, int variance) {
    }

    public boolean isBioComplete(Long userId) {
        return bioRepository.findById(userId)
                .map(this::hasAllPositiveScores)
                .orElse(false);
    }

    private boolean hasAllPositiveScores(UserBio bio) {
        return bio.getVisionaryScore() > 0
                && bio.getChallengerScore() > 0
                && bio.getArchitectScore() > 0
                && bio.getHarmonizerScore() > 0
                && bio.getExplorerScore() > 0
                && bio.getExecutorScore() > 0;
    }
}
