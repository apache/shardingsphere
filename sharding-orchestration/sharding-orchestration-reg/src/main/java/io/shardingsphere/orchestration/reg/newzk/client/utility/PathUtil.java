/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.orchestration.reg.newzk.client.utility;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Path util.
 *
 * @author lidongbo
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathUtil {
    
    /**
     * Get real path.
     *
     * @param root root
     * @param path path
     * @return real path
     */
    public static String getRealPath(final String root, final String path) {
        return adjustPath(root, path);
    }
    
    private static String adjustPath(final String root, final String path) {
        if (Strings.isNullOrEmpty(path)) {
            throw new IllegalArgumentException("path should have content!");
        }
        String rootPath = root;
        if (!root.startsWith(ZookeeperConstants.PATH_SEPARATOR)) {
            rootPath = ZookeeperConstants.PATH_SEPARATOR + root;
        }
        String realPath = path;
        if (!path.startsWith(ZookeeperConstants.PATH_SEPARATOR)) {
            realPath = ZookeeperConstants.PATH_SEPARATOR + path;
        }
        if (!realPath.startsWith(rootPath)) {
            return rootPath + realPath;
        }
        return realPath;
    }
    
    /**
     * Get path nodes, child to root.
     *
     * @param root root
     * @param path path
     * @return all path nodes
     */
    public static Stack<String> getPathReverseNodes(final String root, final String path) {
        String realPath = adjustPath(root, path);
        Stack<String> pathStack = new Stack<>();
        int index = 1;
        int position = realPath.indexOf(ZookeeperConstants.PATH_SEPARATOR, index);
        do {
            pathStack.push(realPath.substring(0, position));
            index = position + 1;
            position = realPath.indexOf(ZookeeperConstants.PATH_SEPARATOR, index);
        }
        while (position > -1);
        pathStack.push(realPath);
        return pathStack;
    }
    
    /**
     * Get path nodes.
     *
     * @param root root
     * @param path path
     * @return all path nodes
     */
    public static List<String> getPathOrderNodes(final String root, final String path) {
        String realPath = adjustPath(root, path);
        List<String> paths = new ArrayList<>();
        int index = 1;
        int position = realPath.indexOf(ZookeeperConstants.PATH_SEPARATOR, index);
    
        do {
            paths.add(realPath.substring(0, position));
            index = position + 1;
            position = realPath.indexOf(ZookeeperConstants.PATH_SEPARATOR, index);
        }
        while (position > -1);
        paths.add(realPath);
        return paths;
    }
    
    /**
     * Get path nodes.
     *
     * @param path path
     * @return all path nodes
     */
    public static List<String> getShortPathNodes(final String path) {
        String realPath = checkPath(path);
        List<String> paths = new ArrayList<>();
        char[] chars = realPath.toCharArray();
        StringBuilder builder = new StringBuilder(ZookeeperConstants.PATH_SEPARATOR);
        for (int i = 1; i < chars.length; i++) {
            if (chars[i] == ZookeeperConstants.PATH_SEPARATOR.charAt(0)) {
                paths.add(builder.toString());
                builder = new StringBuilder(ZookeeperConstants.PATH_SEPARATOR);
                continue;
            }
            builder.append(chars[i]);
            if (i == chars.length - 1) {
                paths.add(builder.toString());
            }
        }
        return paths;
    }
    
    /**
     * Ignore invalid char and // /./  /../.
     * code consult zookeeper
     *
     * @param key key
     * @return real path
     * @throws IllegalArgumentException IllegalArgumentException
     */
    // CHECKSTYLE:OFF
    public static String checkPath(final String key) {
        // CHECKSTYLE:ON
        Preconditions.checkNotNull(key, "path should not be null");

        String path = key;
        if (path.charAt(0) != 47 || path.charAt(path.length() - 1) == 47) {
            path = ZookeeperConstants.PATH_SEPARATOR + path;
        }
    
        if (path.charAt(path.length() - 1) == 47) {
            path = ZookeeperConstants.PATH_SEPARATOR + path;
        }

        char previous = 47;
        char[] chars = path.toCharArray();
        StringBuilder builder = new StringBuilder();
        builder.append(previous);
        
        for (int i = 1; i < chars.length; ++i) {
            char c = chars[i];
            if (c == 0 || (c == 47 && previous == 47)) {
                continue;
            }
            if (c == 46) {
                // ignore /./  /../
                boolean preWarn = previous == 47 || (previous == 46 && chars[i - 2] == 47);
                if (previous == 47 && (i + 1 == chars.length || chars[i + 1] == 47)) {
                    // CHECKSTYLE:OFF
                    i++;
                    continue;
                }
                if ((previous == 46 && chars[i - 2] == 47) && (i + 1 == chars.length || chars[i + 1] == 47)) {
                    i += 2;
                    continue;
                }
            }
            
            if (c > 0 && c < 31 || c > 127 && c < 159 || c > '\ud800' && c < '\uf8ff' || c > '\ufff0' && c < '\uffff') {
                // CHECKSTYLE:ON
                continue;
            }
    
            builder.append(c);
            previous = c;
        }
        return builder.toString();
    }
}
