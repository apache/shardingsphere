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

package org.apache.shardingsphere.test.integration.discovery.cases.base;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.discovery.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.test.integration.discovery.framework.container.compose.BaseContainerComposer;
import org.apache.shardingsphere.test.integration.discovery.framework.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.test.integration.discovery.framework.parameter.DiscoveryParameterized;

import javax.sql.DataSource;
import java.util.List;

/**
 * Base integration test.
 */
@Slf4j
@Getter(AccessLevel.PROTECTED)
public abstract class BaseITCase {
    
    protected static final IntegrationTestEnvironment ENV = IntegrationTestEnvironment.getInstance();
    
    private final BaseContainerComposer containerComposer;
    
    private final DatabaseType databaseType;
    
    private final List<DataSource> mappedDataSources;
    
    private final DataSource proxyDataSource;
    
    public BaseITCase(final DiscoveryParameterized discoveryParameterized) {
        databaseType = discoveryParameterized.getDatabaseType();
        containerComposer = new DockerContainerComposer(discoveryParameterized.getScenario(), discoveryParameterized.getDatabaseType(), discoveryParameterized.getStorageContainerImage());
        containerComposer.start();
        mappedDataSources = containerComposer.getMappedDatasource();
        proxyDataSource = containerComposer.getProxyDatasource();
    }
}
