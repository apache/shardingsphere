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

package org.apache.shardingsphere.mode.metadata.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

/**
 * Active version checker.
 */
@RequiredArgsConstructor
@Slf4j
public final class ActiveVersionChecker {
    
    private final PersistRepository repository;
    
    /**
     * Check whether the current version same with an active version.
     *
     * @param event data changed event
     * @return same or not
     */
    public boolean checkSame(final DataChangedEvent event) {
        return checkSame(event.getValue(), event.getKey());
    }
    
    /**
     * Check whether the current version same with an active version.
     *
     * @param versionNodePath version node path
     * @param currentVersion current version
     * @return same or not
     */
    public boolean checkSame(final VersionNodePath versionNodePath, final int currentVersion) {
        return checkSame(String.valueOf(currentVersion), versionNodePath.getActiveVersionPath());
    }
    
    private boolean checkSame(final String currentVersion, final String activeVersionPath) {
        if (currentVersion.equals(repository.query(activeVersionPath))) {
            return true;
        }
        log.warn("Invalid active version `{}` of key `{}`", currentVersion, activeVersionPath);
        return false;
    }
}
