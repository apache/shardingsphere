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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.node;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cache node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheNode {
    
    private static final String CACHE_NODE = "cache";
    
    /**
     * Get cache path.
     *
     * @param path path
     * @return cache path
     */
    public static String getCachePath(final String path) {
        return Joiner.on("/").join(path, CACHE_NODE);
    }
    
    /**
     * Get cache id by cache path.
     * 
     * @param path patch
     * @param cachePath cache path
     * @return cache id
     */
    public static Optional<String> getCacheId(final String path, final String cachePath) {
        Pattern pattern = Pattern.compile(getCachePath(path) + "/(\\w+)?", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(cachePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
