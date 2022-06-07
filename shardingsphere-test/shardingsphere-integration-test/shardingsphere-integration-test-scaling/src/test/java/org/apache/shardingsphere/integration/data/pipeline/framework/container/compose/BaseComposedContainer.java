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

package org.apache.shardingsphere.integration.data.pipeline.framework.container.compose;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.factory.DatabaseContainerFactory;
import org.apache.shardingsphere.integration.data.pipeline.framework.container.database.DockerDatabaseContainer;
import org.apache.shardingsphere.test.integration.framework.container.atomic.ITContainers;
import org.apache.shardingsphere.test.integration.util.NetworkAliasUtil;
import org.testcontainers.lifecycle.Startable;

@Getter
public abstract class BaseComposedContainer implements Startable {
    
    private final ITContainers containers;
    
    private final DockerDatabaseContainer databaseContainer;
    
    public BaseComposedContainer(final DatabaseType databaseType, final String dockerImageName) {
        this.containers = new ITContainers("");
        this.databaseContainer = containers.registerContainer(DatabaseContainerFactory.newInstance(databaseType, dockerImageName), NetworkAliasUtil.getNetworkAlias("db"));
    }
    
    /**
     * Get proxy jdbc url.
     *
     * @param databaseName database name
     * @return proxy jdbc url
     */
    public abstract String getProxyJdbcUrl(String databaseName);
    
    @Override
    public void start() {
        getContainers().start();
    }
    
    @Override
    public void stop() {
        getContainers().stop();
    }
}
