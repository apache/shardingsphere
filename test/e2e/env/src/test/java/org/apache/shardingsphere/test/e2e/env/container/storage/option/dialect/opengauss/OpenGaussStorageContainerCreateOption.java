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

package org.apache.shardingsphere.test.e2e.env.container.storage.option.dialect.opengauss;

import org.apache.shardingsphere.test.e2e.env.container.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerCreateOption;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Storage container craete option for openGauss.
 */
public final class OpenGaussStorageContainerCreateOption implements StorageContainerCreateOption {
    
    @Override
    public int getPort() {
        return 5432;
    }
    
    @Override
    public String getDefaultImageName() {
        return "opengauss/opengauss:3.1.0";
    }
    
    @Override
    public String getCommand() {
        return "";
    }
    
    @Override
    public Map<String, String> getEnvironments() {
        return Collections.singletonMap("GS_PASSWORD", StorageContainerConstants.OPERATION_PASSWORD);
    }
    
    @Override
    public Collection<String> getMountedConfigurationResources() {
        return Arrays.asList("/usr/local/opengauss/share/postgresql/postgresql.conf.sample", "/usr/local/opengauss/share/postgresql/pg_hba.conf.sample");
    }
    
    @Override
    public Collection<String> getAdditionalEnvMountedSQLResources(final int majorVersion) {
        return Collections.emptyList();
    }
    
    @Override
    public List<Integer> getSupportedMajorVersions() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean withPrivilegedMode() {
        return true;
    }
    
    @Override
    public Optional<String> getDefaultDatabaseName(final int majorVersion) {
        return Optional.of(StorageContainerConstants.OPERATION_USER);
    }
    
    @Override
    public long getStartupTimeoutSeconds() {
        return 120L;
    }
}
