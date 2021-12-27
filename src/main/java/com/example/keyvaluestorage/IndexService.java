package com.example.keyvaluestorage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexService {
    private Map<String, Long> keysOffsets = new HashMap<>();

    public Long getOffset(String key) {
        return keysOffsets.get(key);
    }

    public void setKeyOffset(String key, Long offset) {
        keysOffsets.put(key, offset);
    }

    public void init(Map<String, Long> keysOffsets) {
        this.keysOffsets = keysOffsets;
    }

    public void delete(String key) {
        keysOffsets.remove(key);
    }

    public Map<String, Long> getKeysOffsets() {
        return keysOffsets;
    }
}
