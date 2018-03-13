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
	public static List<String> getAllFilePaths(final File filePath, final String prefixFile, final String suffix) {
		List<String> result = new ArrayList<>();
		File[] files = filePath.listFiles();
		if (files == null) {
			return result;
		}
		for (File each : files) {
			if (each.isDirectory()) {
				getSubFilePaths(each, result, prefixFile, suffix);
			} else {
				getFiles(prefixFile, suffix, result, each);

			}
		}
		return result;
	}

	private static void getFiles(final String prefixFile, final String suffix, final List<String> filePaths, final File f) {
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

	private static List<String> getSubFilePaths(final File filePath, final List<String> filePaths, final String prefixFile,
			final String suffix) {
		File[] files = filePath.listFiles();
		List<String> result = filePaths;
		if (files == null) {
			return result;
		}
		for (File each : files) {
			if (each.isDirectory()) {
				getSubFilePaths(each, result, prefixFile, suffix);
			} else {
				getFiles(prefixFile, suffix, result, each);
			}
		}
		return result;
	}

}
