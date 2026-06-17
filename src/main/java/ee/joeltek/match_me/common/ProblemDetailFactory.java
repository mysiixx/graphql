package ee.joeltek.match_me.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.OffsetDateTime;

@Component
public class ProblemDetailFactory {

    public ProblemDetail create(HttpStatus status, String title, String detail, String path) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(path));
        problem.setProperty("timestamp", OffsetDateTime.now());

        return problem;
    }

    public <T> ProblemDetail create(HttpStatus status, String title, String detail, String path, T messages) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(path));
        problem.setProperty("errors", messages);
        problem.setProperty("timestamp", OffsetDateTime.now());

        return problem;
    }
}
