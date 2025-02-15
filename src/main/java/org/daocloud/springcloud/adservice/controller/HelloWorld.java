package org.daocloud.springcloud.adservice.controller;

import com.alibaba.nacos.common.utils.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
public class HelloWorld {
    private final Environment environment;

    private static AtomicLong count = new AtomicLong();

    private static Long limit = 1L;

    public HelloWorld(Environment environment) {
        this.environment = environment;
    }

    @RequestMapping("/")
    public Mono<String> helloWorld(){
        return Mono.just("adservice-springcloud: hello world!");
    }

    @RequestMapping("/test1")
    public Mono<String> test1(){
        return Mono.just("This is a test 1 API.");
    }

    @RequestMapping("/test2")
    public Mono<String> test2(){
        return Mono.just("This is a test 2 API.");
    }

    @RequestMapping("/timeout/{timeout}")
    public Mono<String> helloWorld(@PathVariable long timeout) throws InterruptedException{
        Thread.sleep(timeout);
        return Mono.just("timeout:"+timeout);
    }

//    @PostMapping("/method")
//    public Mono<String> MethodPost(){
//        return Mono.just("method:"+)
//    }

    @RequestMapping("/method")
    public Mono<String> method(ServerHttpRequest request){
        return Mono.just("method:"+request.getMethodValue());
    }

    @RequestMapping("/hostname")
    public Mono<String> hostname() throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        String hostName = localHost.getHostName();

        return Mono.just("hostname:"+hostName);
    }

    @RequestMapping("/ip")
    public Mono<String> ip() throws UnknownHostException {
        InetAddress localHost = InetAddress.getLocalHost();
        String address = localHost.getHostAddress();
        return Mono.just("ip address:"+address);
    }


    @RequestMapping({"/path/**","/path**"})
    public Mono<String> path(ServerHttpRequest request){
        return Mono.just("path:"+ request.getPath());
    }

    @RequestMapping("/set-retry-count/{limit}")
    public Mono<String> retryCount(@PathVariable long limit){
        HelloWorld.limit = limit;
        count = new AtomicLong();
        return Mono.just("retry-count-limit:"+limit);
    }

    @RequestMapping("/retry")
    public Mono<String> retry(ServerHttpResponse response){
        if (limit <1 || count.addAndGet(1) % limit ==0){
            count = new AtomicLong();
            return Mono.just("retry:"+"success");
        }
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        return Mono.just("retry fai,count is:"+ count.get());
    }

    @RequestMapping("/request-header")
    public Mono<Map<String,List<String>>> requestHeader(ServerHttpRequest request, @RequestParam(required = false) List<String> header){
        if (CollectionUtils.isEmpty(header)){
            return Mono.empty();
        }
        Map<String, List<String>> headers = new HashMap<>();
        for (String hea : header) {
            headers.put(hea, request.getHeaders().get(hea));
        }
        return Mono.just(headers);
    }

    @RequestMapping("/response-header")
    public Mono<Map<String,List<String>>> responseHeader(ServerHttpResponse response, @RequestParam(required = false) List<String> header){
        if (CollectionUtils.isEmpty(header)){
            return Mono.empty();
        }
        Map<String, List<String>> headers = new HashMap<>();
        for (String hea : header) {
            headers.put(hea, response.getHeaders().get(hea));
        }
        return Mono.just(headers);
    }
}
