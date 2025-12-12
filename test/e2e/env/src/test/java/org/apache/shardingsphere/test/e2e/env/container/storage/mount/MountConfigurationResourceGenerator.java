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

package org.apache.shardingsphere.test.e2e.env.container.storage.mount;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerCreateOption;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Mount configuration resource generator.
 */
@RequiredArgsConstructor
public final class MountConfigurationResourceGenerator {
    
    private final DatabaseType databaseType;
    
    private final StorageContainerCreateOption option;
    
    /**
     * Generate mount configuration resource map.
     *
     * @param majorVersion major version
     * @param scenario scenario
     * @return generated resource map
     */
    public Map<String, String> generate(final int majorVersion, final String scenario) {
        Collection<String> mountedConfigResources = option.getMountedConfigurationResources();
        Map<String, String> result = new HashMap<>(mountedConfigResources.size(), 1F);
        for (String each : mountedConfigResources) {
            String fileName = new File(each).getName();
            String configFile = option.getSupportedMajorVersions().isEmpty()
                    ? String.format("container/%s/cnf/%s", databaseType.getType().toLowerCase(), fileName)
                    : String.format("container/%s/cnf/%d/%s", databaseType.getType().toLowerCase(), majorVersion, fileName);
            result.put(getToBeMountedConfigurationFile(configFile, scenario), each);
        }
        return result;
    }
    
    private String getToBeMountedConfigurationFile(final String toBeMountedConfigFile, final String scenario) {
        String scenarioConfigFilePath = String.format("scenario/%s/%s", scenario, toBeMountedConfigFile);
        if (null != Thread.currentThread().getContextClassLoader().getResource(scenarioConfigFilePath)) {
            return "/" + scenarioConfigFilePath;
        }
        String envConfigFilePath = String.format("env/%s", toBeMountedConfigFile);
        if (null != Thread.currentThread().getContextClassLoader().getResource(envConfigFilePath)) {
            return "/" + envConfigFilePath;
        }
        return "/" + toBeMountedConfigFile;
    }
}
