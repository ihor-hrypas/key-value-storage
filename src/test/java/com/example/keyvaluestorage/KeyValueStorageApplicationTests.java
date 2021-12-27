package com.example.keyvaluestorage;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
class KeyValueStorageApplicationTests {
	@Autowired
	private LogFileService logFileService;
	@Autowired
	private StorageService storageService;
	@Autowired
	private IndexService indexService;


	@Test
	void contextLoads() throws IOException {
		Long offset = logFileService.writeValue(new KeyValue("test", "value1"));

		Long offset2 =logFileService.writeValue(new KeyValue("test2", "value2"));

		Long offset3 = logFileService.writeValue(new KeyValue("test", "value3"));

		String str = new Gson().toJson(new KeyValue("test2", "value2"));
		KeyValue keyValue = new Gson().fromJson(str, KeyValue.class);

		System.out.println(">>>>>>>>>>>>>>>>>> " + logFileService.readKeyValueByOffset(offset2));
		System.out.println(">>>>>>>>>>>>>>>>>> " + logFileService.readKeyValueByOffset(offset3));

	}

	@Test
	void storageTest() throws IOException {
		KeyValue value = storageService.read("test");

		System.out.println(">>>>>>>>>>>>>>>>>> " + value);
	}

	@Test
	void storageTest2() throws Exception {
		KeyValue value = storageService.read("test");

		storageService.write("test", "value1");
		storageService.write("test2", "value2");
		storageService.write("test3", "value3");
		storageService.write("test", "value4");

		storageService.delete("test2");
		storageService.update("test3", "value5");

		Map<String, Long> res = logFileService.compressFileValues(indexService.getKeysOffsets());
	}


}
