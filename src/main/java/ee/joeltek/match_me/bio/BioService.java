package ee.joeltek.match_me.bio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ee.joeltek.match_me.common.ResourceNotFoundException;
import ee.joeltek.match_me.profile.ProfileAccessService;
import ee.joeltek.match_me.profile.UserProfileService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BioService {
    private final BioRepository bioRepository;
    private final ProfileAccessService profileAccessService;
    private final UserProfileService profileService;
    
    public BioResponse getMyBio(Long userId) {
        return fetchBioResponse(userId);
    }

    public BioResponse getUserBio(Long userId, Long requesterUserId) {
        if (!profileAccessService.canViewUser(requesterUserId, userId)) 
            throw new ResourceNotFoundException("User not found");

        return fetchBioResponse(userId);
    }

    public Map<String, String> submitBio(Long userId, BioRequest request) {
        UserBio bio = bioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bio not found for user: " + userId));

        Archetype winningArchetype = updateBioScores(bio, request.getAnswers());
        bioRepository.save(bio);
        profileService.updateArchetype(userId, winningArchetype.name());

        return Map.of(
                "message", "Bio processed successfully!",
                "archetype", winningArchetype.name());
    }

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

    private BioResponse fetchBioResponse(Long userId) {
        UserBio bio = bioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bio not found for user: " + userId));

        return new BioResponse(
            bio.getUserId(),
            bio.getVisionaryScore(),
            bio.getChallengerScore(),
            bio.getArchitectScore(),
            bio.getHarmonizerScore(),
            bio.getExplorerScore(),
            bio.getExecutorScore()
        );
    }
}
