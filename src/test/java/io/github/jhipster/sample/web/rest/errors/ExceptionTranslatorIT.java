package io.github.jhipster.sample.web.rest.errors;

import io.github.jhipster.sample.JhipsterSampleApplicationApp;
import io.github.jhipster.sample.web.rest.errors.handlers.ProblemHandler;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.zalando.problem.Problem;

import javax.inject.Inject;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests exception conditions.
 */
@MicronautTest(application = JhipsterSampleApplicationApp.class)
public class ExceptionTranslatorIT {

    @Inject @Client("/")
    RxHttpClient client;

    @Test
    public void testConcurrencyFailure() throws Exception {
        HttpResponse<String> response = client.exchange(HttpRequest.GET("/test/concurrency-failure"), Argument.of(String.class), Argument.of(Problem.class)).onErrorReturn(t -> (HttpResponse<String>) ((HttpClientResponseException) t).getResponse()).blockingFirst();

        assertThat(response.status().getCode()).isEqualTo(409);
        assertThat(response.getContentType()).hasValue(ProblemHandler.PROBLEM);
        assertThat(response.getBody(Problem.class).get().getParameters().get("message")).isEqualTo(ErrorConstants.ERR_CONCURRENCY_FAILURE);
    }


    @Test
    public void testMethodArgumentNotValid() throws Exception {

        HttpResponse<String> response = client.exchange(HttpRequest.POST("/test/method-argument", new TestDTO()).contentType(MediaType.APPLICATION_JSON_TYPE), Argument.of(String.class), Argument.of(Problem.class)).onErrorReturn(t -> (HttpResponse<String>) ((HttpClientResponseException) t).getResponse()).blockingFirst();
        Problem problem = response.getBody(Problem.class).get();
        List<Map> fieldErrors = (List<Map>) problem.getParameters().get("fieldErrors");

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());
        assertThat(response.getContentType()).hasValue(ProblemHandler.PROBLEM);
        assertThat(problem.getParameters().get("message")).isEqualTo(ErrorConstants.ERR_VALIDATION);
        assertThat(fieldErrors.get(0).get("objectName")).isEqualTo("testDTO");
        assertThat(fieldErrors.get(0).get("field")).isEqualTo("test");
        assertThat(fieldErrors.get(0).get("message")).isEqualTo("must not be null");
    }

    @Test
    public void testMissingRequestPartException() throws Exception {
        HttpResponse<String> response = client.exchange(HttpRequest.GET("/test/missing-servlet-request-part"), Argument.of(String.class), Argument.of(Problem.class)).onErrorReturn(t -> (HttpResponse<String>) ((HttpClientResponseException) t).getResponse()).blockingFirst();
        Problem problem = response.getBody(Problem.class).get();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());
        assertThat(response.getContentType()).hasValue(ProblemHandler.PROBLEM);
        assertThat(problem.getParameters().get("message")).isEqualTo("error.http.400");
    }

    @Test
    public void testMissingServletRequestParameterException() throws Exception {
        HttpResponse<String> response = client.exchange(HttpRequest.GET("/test/missing-servlet-request-parameter"), Argument.of(String.class), Argument.of(Problem.class)).onErrorReturn(t -> (HttpResponse<String>) ((HttpClientResponseException) t).getResponse()).blockingFirst();
        Problem problem = response.getBody(Problem.class).get();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());
        assertThat(response.getContentType()).hasValue(ProblemHandler.PROBLEM);
        assertThat(problem.getParameters().get("message")).isEqualTo("error.http.400");
    }

/*
    @Test
    public void testAccessDenied() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
            .andExpect(status().isForbidden())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.http.403"))
            .andExpect(jsonPath("$.detail").value("test access denied!"));
    }*/

    @Test
    public void testUnauthorized() throws Exception {
        HttpResponse<String> response = client.exchange(HttpRequest.GET("/test/unauthorized"), Argument.of(String.class), Argument.of(Problem.class)).onErrorReturn(t -> (HttpResponse<String>) ((HttpClientResponseException) t).getResponse()).blockingFirst();
        Problem problem = response.getBody(Problem.class).get();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.UNAUTHORIZED.getCode());
        assertThat(response.getContentType()).hasValue(ProblemHandler.PROBLEM);
        assertThat(problem.getParameters().get("message")).isEqualTo("error.http.401");
        assertThat(problem.getParameters().get("path")).isEqualTo("/test/unauthorized");
    }

    @Test
    public void testMethodNotSupported() throws Exception {

        HttpResponse<String> response = client.exchange(HttpRequest.POST("/test/access-denied", ""), Argument.of(String.class), Argument.of(Problem.class)).onErrorReturn(t -> (HttpResponse<String>) ((HttpClientResponseException) t).getResponse()).blockingFirst();
        Problem problem = response.getBody(Problem.class).get();

        assertThat(response.status().getCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.getCode());
        assertThat(response.getContentType()).hasValue(ProblemHandler.PROBLEM);
        assertThat(problem.getParameters().get("message")).isEqualTo("error.http.405");
        assertThat(problem.getParameters().get("path")).isEqualTo("/test/unauthorized");

/*        mockMvc.perform(post("/test/access-denied"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.http.405"))
            .andExpect(jsonPath("$.detail").value("Request method 'POST' not supported"));*/
    }
/*
    @Test
    public void testExceptionWithResponseStatus() throws Exception {
        mockMvc.perform(get("/test/response-status"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.http.400"))
            .andExpect(jsonPath("$.title").value("test response status"));
    }

    @Test
    public void testInternalServerError() throws Exception {
        mockMvc.perform(get("/test/internal-server-error"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.message").value("error.http.500"))
            .andExpect(jsonPath("$.title").value("Internal Server Error"));
    }
*/
}
