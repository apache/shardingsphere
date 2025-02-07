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

package org.apache.shardingsphere.mode.node.path.version;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Version node path.
 */
@RequiredArgsConstructor
public final class VersionNodePath {
    
    public static final String VERSION_PATTERN = "(\\d+)";
    
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
     * Judge whether to active version path.
     *
     * @param path to be judged path
     * @return is active version path or not
     */
    public boolean isActiveVersionPath(final String path) {
        Pattern pattern = Pattern.compile(getActiveVersionPath() + "$", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(path).find();
    }
    
    /**
     * Find identifier name by active version path.
     *
     * @param activeVersionPath active version path
     * @param identifierGroupIndex identifier group index
     * @return found identifier
     */
    public Optional<String> findIdentifierByActiveVersionPath(final String activeVersionPath, final int identifierGroupIndex) {
        Pattern pattern = Pattern.compile(getActiveVersionPath() + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(activeVersionPath);
        return matcher.find() ? Optional.of(matcher.group(identifierGroupIndex)) : Optional.empty();
    }
    
    /**
     * Judge whether to version path.
     *
     * @param path to be judged path
     * @return is version path or not
     */
    public boolean isVersionPath(final String path) {
        Pattern pattern = Pattern.compile(String.join("/", getVersionsPath(), VERSION_PATTERN) + "$", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(path).find();
    }
    
    /**
     * Find identifier name by versions' path.
     *
     * @param versionsPath versions path
     * @param identifierGroupIndex identifier group index
     * @return found identifier
     */
    public Optional<String> findIdentifierByVersionsPath(final String versionsPath, final int identifierGroupIndex) {
        Pattern pattern = Pattern.compile(getVersionsPath(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(versionsPath);
        return matcher.find() ? Optional.of(matcher.group(identifierGroupIndex)) : Optional.empty();
    }
}
