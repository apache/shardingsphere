package org.apache.shardingsphere.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * check duplicate class.
 *
 * @author zhaojinchao
 */
@Slf4j
public final class ShardingVersion {

    /**
     * check duplicate class default false.
     *
     * @param clz classLoader
     */
    public static void checkDuplicateClass(final Class<?> clz) {
        checkDuplicateClass(clz, false);
    }

    /**
     * get class path.
     *
     * @param clz classLoader
     * @param throwsException need throw exception
     */
    public static void checkDuplicateClass(final Class<?> clz, final boolean throwsException) {
        checkDuplicateClass(clz.getName().replace('.', '/') + ".class", throwsException);
    }

    private static void checkDuplicateClass(final String classPath, final boolean throwsException) {
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

    private static ClassLoader getClassLoader(final Class clz) {
        return clz.getClassLoader();
    }

}
