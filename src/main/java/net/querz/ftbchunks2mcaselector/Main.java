package net.querz.ftbchunks2mcaselector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class Main {

	public static void main(String[] args) throws IOException {

		if (args.length != 2) {
			throw new IllegalArgumentException("invalid number of arguments. required are: <directory> <dimension>");
		}

		File dir = new File(args[0]);
		if (!dir.isDirectory() || !dir.exists()) {
			throw new FileNotFoundException("directory " + dir + " does not exist or is not a directory");
		}

		File[] files = dir.listFiles((d, f) -> f.matches("^[0-9a-f]{32}.*\\.json$"));
		if (files == null || files.length == 0) {
			throw new FileNotFoundException("no matching files found in " + dir);
		}

		int i = 0;

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output.csv")))) {
			for (File jsonFile : files) {
				JSONObject object;
				String json = new String(Files.readAllBytes(jsonFile.toPath()));
				try {
					 object = new JSONObject(json);
				} catch (JSONException ex) {
					System.out.println("failed to parse json file " + jsonFile);
					ex.printStackTrace();
					continue;
				}
				if (!object.has("chunks")) {
					System.out.println("no chunks in " + jsonFile + ", skipping");
					continue;
				}
				JSONObject chunks = object.getJSONObject("chunks");
				if (!chunks.has(args[1])) {
					System.out.println("no chunks for dimension " + args[1] + " in " + jsonFile + ", skipping");
					continue;
				}
				JSONArray dimension = chunks.getJSONArray(args[1]);
				for (Object c : dimension) {
					if (!(c instanceof JSONObject)) {
						System.out.println("chunk coordinate in list is not a JSONObject in " + jsonFile);
						continue;
					}
					JSONObject chunk = (JSONObject) c;
					try {
						int chunkX = chunk.getInt("x");
						int chunkZ = chunk.getInt("z");
						int regionX = chunkX >> 5;
						int regionZ = chunkZ >> 5;
						bw.write(regionX + ";" + regionZ + ";" + chunkX + ";" + chunkZ + "\n");
						i++;
					} catch (Exception ex) {
						System.out.println("missing chunk coordinates in " + jsonFile);
					}
				}
			}
		}

		System.out.println("converted " + i + " chunks.");
	}
}
