/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.dbtest.common;

import io.shardingjdbc.dbtest.exception.DbTestException;

public class PathUtil {

    private static final String BASEPATH = PathUtil.class.getClassLoader().getResource("").getPath();

    /**
     * Get the resource path.
     *
     * @param path path
     * @param parent  parent path
     * @return path
     */
    public static String getPath(final String path, final String parent) {
        if (path == null) {
            throw new DbTestException("The path cannot be empty");
        }

        String result = path;
        if (result.startsWith("classpath:")) {
            result = result.substring("classpath:".length());
            result = BASEPATH + result;
            return result;
        }
        if (parent != null) {
            return parent + result;
        }
        return result;
    }

    /**
     * Get the resource path.
     *
     * @param path path
     * @return path
     */
    public static String getPath(final String path) {
        return getPath(path, null);
    }

}
