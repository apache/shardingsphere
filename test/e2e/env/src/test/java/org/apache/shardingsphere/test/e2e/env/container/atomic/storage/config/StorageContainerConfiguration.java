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

package org.apache.shardingsphere.test.e2e.env.container.atomic.storage.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;

/**
 * Docker storage container configuration.
 */
@RequiredArgsConstructor
@Getter
public class StorageContainerConfiguration {
    
    private final String scenario;
    
    private final String containerCommand;
    
    private final Map<String, String> containerEnvironments;
    
    private final Map<String, String> mountedResources;
    
    private final Collection<String> databaseNames;
    
    private final Collection<String> expectedDatabaseNames;
    
    public StorageContainerConfiguration(final String containerCommand, final Map<String, String> containerEnvironments, final Map<String, String> mountedResources,
                                         final Collection<String> databaseNames, final Collection<String> expectedDatabaseNames) {
        this.databaseNames = databaseNames;
        this.expectedDatabaseNames = expectedDatabaseNames;
        this.scenario = null;
        this.containerCommand = containerCommand;
        this.containerEnvironments = containerEnvironments;
        this.mountedResources = mountedResources;
    }
}
