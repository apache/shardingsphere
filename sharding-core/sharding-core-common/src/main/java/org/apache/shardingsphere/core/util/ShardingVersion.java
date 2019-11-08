package org.apache.shardingsphere.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhaojinchao
 */
@Slf4j
public final class ShardingVersion {

	public static void checkDuplicateClass(Class<?> claz) {
		checkDuplicateClass(claz, false);
	}

	public static void checkDuplicateClass(Class<?> clz, boolean throwsException) {
		checkDuplicateClass(clz.getName().replace('.', '/') + ".class", throwsException);
	}

	/**
	 * check duplicate class of this project
	 *
	 * @param classPath
	 * @param throwsException
	 */
	public static void checkDuplicateClass(String classPath, boolean throwsException) {
		try {
			Set<String> files = new HashSet<>();
			Enumeration<URL> urls = getClassLoader(ShardingVersion.class).getResources(classPath);
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				if (null != url) {
					String file = url.getFile();
					if (!StringUtil.isEmpty(file)) {
						files.add(file);
					}
				}
			}
			if (files.size() > 1) {
				String error = "Duplicate class " + classPath;
				if (throwsException) {
					throw new IllegalStateException(error);
				} else {
					log.info(error);
				}
			}
		} catch (IOException e) {
			log.info(e.getMessage(), e);
		}
	}

	/**
	 * get classLoader
	 *
	 * @param clz
	 * @return
	 */
	public static ClassLoader getClassLoader(Class clz) { return clz.getClassLoader(); }


}
