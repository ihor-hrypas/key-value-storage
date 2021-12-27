package com.example.keyvaluestorage.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {
    private Long maxWeight;
}
