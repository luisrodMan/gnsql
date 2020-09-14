package com.gnsys.gnsql;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
	
	public static void save(String data, String path) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)))) {
			writer.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
