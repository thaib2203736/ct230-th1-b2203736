package vn.edu.bookstore.order.client;

import java.net.http.HttpClient;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.loadbalancer.DeferringLoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class CatalogClientConfig {

    @Bean
    RestClient catalogRestClient(RestClient.Builder builder, CatalogClientProperties props,
                                 ObjectProvider<DeferringLoadBalancerInterceptor> loadBalancer) {
        HttpClient http = HttpClient.newBuilder().connectTimeout(props.connectTimeout()).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(props.readTimeout());
        builder.baseUrl(props.baseUrl()).requestFactory(factory);
        if (props.loadBalanced()) {
            loadBalancer.ifAvailable(builder::requestInterceptor);
        }
        return builder.build();
    }

    @Bean
    Customizer<Resilience4JCircuitBreakerFactory> catalogCircuitBreaker(CatalogClientProperties props) {
        // TODO(TH3-3): factory.configure(b -> b.circuitBreakerConfig(CircuitBreakerConfig.custom()
        //   .slidingWindowSize(..).minimumNumberOfCalls(..).failureRateThreshold(..)
        //   .waitDurationInOpenState(..).ignoreExceptions(UnknownBookException.class).build()), "catalog");
        //   và ghi log mỗi lần cầu dao chuyển trạng thái (addCircuitBreakerCustomizer + onStateTransition)
        return factory -> { };
    }
}
