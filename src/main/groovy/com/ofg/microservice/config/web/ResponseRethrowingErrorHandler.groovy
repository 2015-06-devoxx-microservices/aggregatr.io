package com.ofg.microservice.config.web
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler

@TypeChecked
@PackageScope
@Slf4j
class ResponseRethrowingErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return RestUtil.isError(response.getStatusCode());
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        String responseBody = response.body?.text
        log.error("Response error: status code [$response.statusCode] body [$responseBody]");
        throw new ResponseException(String.format("Body: [%s]", responseBody));
    }

}
