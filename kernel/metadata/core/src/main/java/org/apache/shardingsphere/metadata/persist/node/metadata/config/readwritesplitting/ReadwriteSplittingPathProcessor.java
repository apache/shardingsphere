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

package org.apache.shardingsphere.metadata.persist.node.metadata.config.readwritesplitting;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Readwrite-splitting path processor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadwriteSplittingPathProcessor {
    
    private static final String ROOT_NODE = "readwrite_splitting";
    
    private static final String ACTIVE_VERSION = "/active_version";
    
    private static final String VERSIONS = "/versions";
    
    /**
     * Get group name.
     *
     * @param path config node path
     * @return group name
     */
    public static Optional<String> getGroupName(final String path) {
        Pattern pattern = Pattern.compile(".+(?<=readwrite_splitting/)(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Get active version.
     *
     * @param path config node path
     * @return group name
     */
    public static Optional<String> getActiveVersion(final String path) {
        Pattern pattern = Pattern.compile(".+(=active_version/)(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(path);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
