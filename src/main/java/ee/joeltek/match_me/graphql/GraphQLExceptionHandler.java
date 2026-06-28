package ee.joeltek.match_me.graphql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import ee.joeltek.match_me.common.BusinessRuleException;
import ee.joeltek.match_me.common.InvalidOperationException;
import ee.joeltek.match_me.common.ResourceExistsException;
import ee.joeltek.match_me.common.ResourceNotFoundException;
import ee.joeltek.match_me.common.UnauthorizedOperationException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;

@Component
public class GraphQLExceptionHandler extends DataFetcherExceptionResolverAdapter {
    private static final Logger log = LoggerFactory.getLogger(GraphQLExceptionHandler.class);

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        return switch(ex) {
            case ResourceNotFoundException      e -> error(env, e.getMessage(), ErrorType.NOT_FOUND);
            case ResourceExistsException        e -> error(env, e.getMessage(), ErrorType.BAD_REQUEST);
            case InvalidOperationException      e -> error(env, e.getMessage(), ErrorType.BAD_REQUEST);
            case UnauthorizedOperationException e -> error(env, e.getMessage(), ErrorType.FORBIDDEN);
            case BusinessRuleException          e -> error(env, e.getMessage(), httpStatusToErrorType(e.getStatus()));
            case IllegalArgumentException e -> 
                error(env,
                    e.getMessage() == null || e.getMessage().isBlank()
                        ? "Request body is missing or malformed"
                        : e.getMessage(),
                    ErrorType.BAD_REQUEST);
            case ResponseStatusException e -> {
                String message = (e.getReason() != null && !e.getReason().isBlank())
                        ? e.getReason()
                        : "The request failed";
                yield error(env, message, httpStatusToErrorType(e.getStatusCode()));
            }
            default -> {
                log.error("Unhandled GraphQL exception", ex);
                yield null;
            }
        };
    }

    private GraphQLError error(DataFetchingEnvironment env, String msg, ErrorType type) {
        return GraphqlErrorBuilder.newError(env)
                .message(msg)
                .errorType(type)
                .build();
    }

    private ErrorType httpStatusToErrorType(HttpStatusCode status) {
        return switch(status.value()) {
            case 400 -> ErrorType.BAD_REQUEST;
            case 401 -> ErrorType.UNAUTHORIZED;
            case 403 -> ErrorType.FORBIDDEN;
            case 404 -> ErrorType.NOT_FOUND;
            default -> ErrorType.INTERNAL_ERROR;
        };
    }
}
