package io.shardingjdbc.dbtest.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件帮助类
 */
public class FileUtils {

	/**
	 * 查找某个目录下的所有文件
	 * 
	 * @param filePath
	 * @param prefixFile
	 * @return
	 */
	public static List<String> getAllFilePaths(File filePath, String prefixFile, String suffix) {
		List<String> filePaths = new ArrayList<>();
		File[] files = filePath.listFiles();
		if (files == null) {
			return filePaths;
		}
		for (File f : files) {
			if (f.isDirectory()) {
				getSubFilePaths(f, filePaths, prefixFile, suffix);
			} else {
				getFiles(prefixFile, suffix, filePaths, f);

			}
		}
		return filePaths;
	}

	private static void getFiles(String prefixFile, String suffix, List<String> filePaths, File f) {
		if (prefixFile != null) {
			if (f.getName().startsWith(prefixFile)) {
				if (suffix != null) {
					if (f.getName().endsWith("." + suffix)) {
						filePaths.add(f.getPath());
					}
				} else {
					filePaths.add(f.getPath());
				}
			}
		} else {
			filePaths.add(f.getPath());
		}
	}

	private static List<String> getSubFilePaths(File filePath, List<String> filePaths, String prefixFile,
			String suffix) {
		File[] files = filePath.listFiles();
		if (files == null) {
			return filePaths;
		}
		for (File f : files) {
			if (f.isDirectory()) {
				getSubFilePaths(f, filePaths, prefixFile, suffix);
			} else {
				getFiles(prefixFile, suffix, filePaths, f);
			}
		}
		return filePaths;
	}

}
