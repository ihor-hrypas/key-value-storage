package com.example.keyvaluestorage;

import com.example.keyvaluestorage.property.CacheProperties;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class StorageService {
    private final LogFileService logFileService;
    private final CacheProperties cacheProperties;
    private final IndexService indexService;

    private Cache<String, String> cache;

    @PostConstruct
    public void init() throws IOException {
        cache = CacheBuilder.newBuilder()
                .maximumWeight(cacheProperties.getMaxWeight())
                .weigher((Weigher<String, String>) (key, value) -> key.length() + value.length())
                .build();
        initValues();
    }

    private void initValues() throws IOException {
        indexService.init(logFileService.readAllOffsets());
    }

    public KeyValue read(String key) throws IOException {
        String valueFromCache = cache.getIfPresent(key);
        if (Objects.nonNull(valueFromCache)) {
            return new KeyValue(key, valueFromCache);
        }
        Long offset = indexService.getOffset(key);
        if (Objects.isNull(offset)) {
            return null;
        }

        return logFileService.readKeyValueByOffset(offset);
    }

    public void write(String key, String value) throws IOException {
        cache.put(key, value);
        long offset = logFileService.writeValue(new KeyValue(key, value));
        indexService.setKeyOffset(key, offset);
    }

    public void delete(String key) throws IOException {
        cache.invalidate(key);
        logFileService.deleteByKey(key);
        indexService.delete(key);
    }

    public void update(String key, String value) throws IOException {
        write(key, value);
    }
}
