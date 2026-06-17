package ee.joeltek.match_me.bio;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class BioRequest {

    @NotNull(message = "Answers cannot be null")
    @Size(min = 18, max = 18, message = "You must provide exactly 18 answers")
    private List<Integer> answers;

}