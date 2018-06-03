package org.dice.factcheckbenchmark.system.api;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @author DANISH AHMED on 5/6/2018
 */
public class Client {
    private MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
    private RestTemplate rest = new RestTemplate();

    private HttpHeaders headers = new HttpHeaders();
    private HttpEntity<MultiValueMap<String, Object>> request;

    private String requestURL = "";

    public Client(MultiValueMap<String, Object> map, MediaType headerMediaType, String requestURL) {
        this.map = map;
        this.headers.setContentType(headerMediaType);
        this.request = new HttpEntity<MultiValueMap<String, Object>>(this.map, this.headers);

        this.requestURL = requestURL;
    }

    public ResponseEntity<FactCheckHobbitResponse> getResponse(HttpMethod httpMethod) {
        return rest.exchange(this.requestURL, httpMethod, request, FactCheckHobbitResponse.class);
    }
}
