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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.dialect;

import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config.option.StorageContainerConfigurationOption;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Storage container configuration option for H2.
 */
public final class H2StorageContainerConfigurationOption implements StorageContainerConfigurationOption {
    
    @Override
    public String getCommand() {
        return "";
    }
    
    @Override
    public Map<String, String> getContainerEnvironments() {
        return Collections.emptyMap();
    }
    
    @Override
    public Collection<String> getMountedConfigurationResources() {
        return Collections.emptyList();
    }
    
    @Override
    public Collection<String> getAdditionalMountedSQLEnvResources(final int majorVersion) {
        return Collections.emptyList();
    }
    
    @Override
    public boolean isEmbeddedStorageContainer() {
        return true;
    }
    
    @Override
    public List<Integer> getSupportedMajorVersions() {
        return Collections.emptyList();
    }
}
