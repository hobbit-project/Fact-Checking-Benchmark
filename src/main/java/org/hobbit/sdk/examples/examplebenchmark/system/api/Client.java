package org.hobbit.sdk.examples.examplebenchmark.system.api;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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


}
