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

package org.apache.shardingsphere.mode.node.path.type.version;

import lombok.RequiredArgsConstructor;

/**
 * Version node path.
 */
@RequiredArgsConstructor
public final class VersionNodePath {
    
    private static final String ACTIVE_VERSION = "active_version";
    
    private static final String VERSIONS = "versions";
    
    private final String path;
    
    /**
     * Get active version path.
     *
     * @return active version path
     */
    public String getActiveVersionPath() {
        return String.join("/", path, ACTIVE_VERSION);
    }
    
    /**
     * Get versions path.
     *
     * @return versions path
     */
    public String getVersionsPath() {
        return String.join("/", path, VERSIONS);
    }
    
    /**
     * Get version path.
     *
     * @param version version
     * @return version path
     */
    public String getVersionPath(final int version) {
        return String.join("/", getVersionsPath(), String.valueOf(version));
    }
    
    /**
     * Get version path.
     *
     * @param activeVersionPath active version path
     * @param activeVersion active version
     * @return version path
     */
    public static String getVersionPath(final String activeVersionPath, final int activeVersion) {
        return String.join("/", activeVersionPath.replace(ACTIVE_VERSION, VERSIONS), String.valueOf(activeVersion));
    }
    
    /**
     *  Is active version path.
     *
     * @param path path
     * @return is active version path or not
     */
    public static boolean isActiveVersionPath(final String path) {
        return path.endsWith(ACTIVE_VERSION);
    }
}
