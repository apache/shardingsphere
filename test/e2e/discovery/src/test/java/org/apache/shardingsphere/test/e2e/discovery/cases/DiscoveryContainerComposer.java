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

package org.apache.shardingsphere.test.e2e.discovery.cases;

import lombok.Getter;
import org.apache.shardingsphere.test.e2e.discovery.framework.container.compose.BaseContainerComposer;
import org.apache.shardingsphere.test.e2e.discovery.framework.container.compose.DockerContainerComposer;
import org.apache.shardingsphere.test.e2e.discovery.framework.parameter.DiscoveryTestParameter;

import javax.sql.DataSource;
import java.util.List;

/**
 * Discovery container composer.
 */
@Getter
public final class DiscoveryContainerComposer implements AutoCloseable {
    
    private final BaseContainerComposer containerComposer;
    
    private final List<DataSource> mappedDataSources;
    
    private final DataSource proxyDataSource;
    
    public DiscoveryContainerComposer(final DiscoveryTestParameter testParam) {
        containerComposer = new DockerContainerComposer(testParam.getScenario(), testParam.getDatabaseType(), testParam.getStorageContainerImage());
        containerComposer.start();
        mappedDataSources = containerComposer.getMappedDatasource();
        proxyDataSource = containerComposer.getProxyDatasource();
    }
    
    @Override
    public void close() {
        containerComposer.stop();
    }
}
