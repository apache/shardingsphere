/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.mode.node.path.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Global properties node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalPropertiesNodePath {
    
    private static final String ROOT_NODE = "/props";
    
    /**
     * Get properties path.
     *
     * @return properties path
     */
    public static String getRootPath() {
        return ROOT_NODE;
    }
    
    /**
     * Get properties version root path.
     *
     * @return properties version root path
     */
    public static String getVersionRootPath() {
        return new VersionNodePath(getRootPath()).getVersionsPath();
    }
    
    /**
     * Get properties version path.
     *
     * @param version version
     * @return properties version path
     */
    public static String getVersionPath(final int version) {
        return new VersionNodePath(getRootPath()).getVersionPath(version);
    }
    
    /**
     * Get properties active version path.
     *
     * @return properties active version path
     */
    public static String getActiveVersionPath() {
        return new VersionNodePath(getRootPath()).getActiveVersionPath();
    }
    
    /**
     * Is properties active version path.
     *
     * @param path path
     * @return true or false
     */
    public static boolean isActiveVersionPath(final String path) {
        Pattern pattern = Pattern.compile(getActiveVersionPath() + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find();
    }
}
