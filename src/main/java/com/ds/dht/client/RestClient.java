package com.ds.dht.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestClient {

    @Autowired
    private RestTemplate restTemplate;

    public <R> ResponseEntity<R> get(String url, ParameterizedTypeReference<R> responseType) {
        return restTemplate.exchange(url,
                HttpMethod.GET, null, responseType);
    }

    public <RQ, RS> ResponseEntity<RS> put(String url, RQ body, ParameterizedTypeReference<RS> responseType) {
        RequestEntity<RQ> requestEntity = RequestEntity
                .put(url)
                .accept(MediaType.APPLICATION_JSON)
                .body(body);
        return restTemplate.exchange(requestEntity, responseType);
    }

    public <RQ, RS> ResponseEntity<RS> put(String url, RQ body) {
        RequestEntity<RQ> requestEntity = RequestEntity
                .put(url)
                .accept(MediaType.APPLICATION_JSON)
                .body(body);
        return restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>() {
        });
    }

}
