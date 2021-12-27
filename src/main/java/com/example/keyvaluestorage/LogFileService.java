package com.example.keyvaluestorage;

import com.example.keyvaluestorage.property.LogProperties;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class LogFileService {
    public static final String DELETED_VALUE = "####";
    private final LogProperties logProperties;

    public KeyValue readKeyValueByOffset(Long offset) throws IOException {
        RandomAccessFile file = new RandomAccessFile(logProperties.getFilename(), "r");
        file.seek(offset);
        String line = file.readLine();
        String key = line.substring(0, line.indexOf("':"));
        String value = line.substring(line.indexOf("':") + 2);
        if (value == DELETED_VALUE) {
            return null;
        }
        return new KeyValue(key, value);
    }

    public Long writeValue(KeyValue keyValue) throws IOException {
        RandomAccessFile file = new RandomAccessFile(logProperties.getFilename(), "rw");
        Long offset = writeValueToFile(keyValue, file);
        file.close();
        return offset;
    }


    private Long writeValueToFile(KeyValue keyValue, RandomAccessFile file) throws IOException {
        long offset = file.length();
        file.seek(offset);
        file.write(String.format("%s':%s\n",keyValue.getKey(), keyValue.getValue()).getBytes(StandardCharsets.UTF_8));
        return offset;
    }

    public void deleteByKey(String key) throws IOException {
        writeValue(new KeyValue(key, DELETED_VALUE));
    }

    public Map<String, Long> readAllOffsets() throws IOException {
        Long offset = 0l;
        HashMap<String, Long> res = new HashMap<String, Long>();
        RandomAccessFile file = new RandomAccessFile(logProperties.getFilename(), "r");
        String line = file.readLine();
        while (StringUtils.hasLength(line)) {
            String key = line.substring(0, line.indexOf("':"));
            res.put(key, offset);

            offset = offset + line.length() + 1l;

            file.seek(offset);
            line = file.readLine();
        }
        return res;
    }

    public Map<String, Long> compressFileValues(Map<String, Long> keysOffsets) throws Exception {
        HashMap<String, Long> res = new HashMap<String, Long>();
        RandomAccessFile oldFile = new RandomAccessFile(logProperties.getFilename(), "r");
        RandomAccessFile newFile = new RandomAccessFile(logProperties.getFilename()+ "1", "rw");
        keysOffsets.entrySet().forEach(entry -> {
            try {
                KeyValue keyValue = readKeyValueByOffset(entry.getValue());
                Long offset = writeValueToFile(keyValue, newFile);
                res.put(keyValue.getKey(), offset);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        oldFile.close();
        newFile.close();

        rewriteFiles();
        return res;
    }

    private void rewriteFiles() {
        File f = new File(logProperties.getFilename());
        f.delete();
        new File(logProperties.getFilename()+ "1")
                .renameTo(new File(logProperties.getFilename()));
    }
}
