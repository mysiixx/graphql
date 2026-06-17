package ee.joeltek.match_me.bio;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BioResponse {
    private Long id;
    private int visionaryScore;
    private int challengerScore;
    private int architectScore;
    private int harmonizerScore;
    private int explorerScore;
    private int executorScore;
}