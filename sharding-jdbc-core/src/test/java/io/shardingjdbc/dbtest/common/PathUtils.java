package io.shardingjdbc.dbtest.common;

import io.shardingjdbc.dbtest.exception.DbTestException;

public class PathUtils {

	private static final String BasePath = PathUtils.class.getClassLoader().getResource("").getPath();

	/**
	 * 获取资源路径
	 * 
	 * @param path
	 * @return
	 */
	public static String getPath(String path, String parent) {
		if (path == null) {
			throw new DbTestException("路径不能为空");
		}

		String result = path;
		if (result.startsWith("classpath:")) {
			result = result.substring("classpath:".length());
			result = BasePath + result;
			return result;
		}
		if (parent != null) {
			return parent + result;
		}
		return result;
	}

	/**
	 * 获取资源路径
	 * 
	 * @param path
	 * @return
	 */
	public static String getPath(String path) {
		return getPath(path, null);
	}

}
