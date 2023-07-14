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

package org.apache.shardingsphere.metadata.persist.service.version;

import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;

import java.util.Collection;

/**
 * Meta data version based registry service.
 */
public interface MetaDataVersionBasedPersistService {
    
    /**
     * Switch active version.
     *
     * @param metaDataVersions meta data versions
     */
    void switchActiveVersion(Collection<MetaDataVersion> metaDataVersions);
    
    /**
     * Get active version by full path.
     *
     * @param fullPath full path
     * @return active version
     */
    String getActiveVersionByFullPath(String fullPath);
    
    /**
     * Get version path by active version.
     *
     * @param path path
     * @param activeVersion active version
     * @return version path
     */
    String getVersionPathByActiveVersion(String path, String activeVersion);
}
