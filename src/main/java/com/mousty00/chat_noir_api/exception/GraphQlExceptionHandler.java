package com.mousty00.chat_noir_api.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GraphQlExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof AuthenticationException ae) {
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.UNAUTHORIZED)
                    .message(ae.getMessage())
                    .build();
        }

        if (ex instanceof ApiException ae) {
            ErrorType errorType = switch (ae.getStatus()) {
                case NOT_FOUND -> ErrorType.NOT_FOUND;
                case FORBIDDEN -> ErrorType.FORBIDDEN;
                case UNAUTHORIZED -> ErrorType.UNAUTHORIZED;
                case BAD_REQUEST -> ErrorType.BAD_REQUEST;
                default -> ErrorType.INTERNAL_ERROR;
            };
            return GraphqlErrorBuilder.newError(env)
                    .errorType(errorType)
                    .message(ae.getMessage())
                    .build();
        }

        if (ex instanceof AccessDeniedException) {
            return GraphqlErrorBuilder.newError(env)
                    .errorType(ErrorType.FORBIDDEN)
                    .message("You don't have permission to access this resource")
                    .build();
        }

        log.error("Unhandled GraphQL exception at {}: {}", env.getField().getName(), ex.getMessage(), ex);
        return null;
    }
}
