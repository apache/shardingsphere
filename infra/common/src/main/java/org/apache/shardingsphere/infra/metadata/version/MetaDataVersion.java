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

package org.apache.shardingsphere.infra.metadata.version;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Meta data version.
 */
@RequiredArgsConstructor
@Getter
public final class MetaDataVersion {
    
    public static final String DEFAULT_VERSION = "0";
    
    private static final String ACTIVE_VERSION = "active_version";
    
    private static final String VERSIONS = "versions";
    
    private final String key;
    
    private final String currentActiveVersion;
    
    private final String nextActiveVersion;
    
    public MetaDataVersion(final String key) {
        this(key, "", "");
    }
    
    /**
     * Get active version node path.
     *
     * @return path of active version node
     */
    public String getActiveVersionNodePath() {
        return String.join("/", key, ACTIVE_VERSION);
    }
    
    /**
     * Get versions node path.
     *
     * @param version version
     * @return path of versions node
     */
    public String getVersionsNodePath(final String version) {
        return String.join("/", key, VERSIONS, version);
    }
    
    /**
     * Get versions path.
     *
     * @return path of versions
     */
    public String getVersionsPath() {
        return String.join("/", key, VERSIONS);
    }
}
