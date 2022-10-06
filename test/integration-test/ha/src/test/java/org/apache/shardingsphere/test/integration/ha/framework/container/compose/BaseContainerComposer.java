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

package org.apache.shardingsphere.test.integration.ha.framework.container.compose;

import lombok.Getter;
import org.apache.shardingsphere.test.integration.env.container.atomic.ITContainers;
import org.testcontainers.lifecycle.Startable;

import javax.sql.DataSource;
import java.util.List;

/**
 * Abstract composed container.
 */
public abstract class BaseContainerComposer implements Startable {
    
    @Getter
    private final ITContainers containers;
    
    private final String scenario;
    
    public BaseContainerComposer(final String scenario) {
        this.scenario = scenario;
        this.containers = new ITContainers(scenario);
    }
    
    /**
     * Get proxy data source.
     *
     * @param databaseName database name
     * @return proxy data source
     */
    public abstract DataSource getProxyDatasource(String databaseName);
    
    /**
     * Get Datasource with storage container exposed port and network alias.
     * 
     * @param databaseName database name
     * @return list of datasource
     */
    public abstract List<DataSource> getExposedDatasource(String databaseName);
    
    /**
     * Get Datasource with storage container mapped port and host.
     *
     * @param databaseName database name
     * @return list of datasource
     */
    public abstract List<DataSource> getMappedDatasource(String databaseName);
    
    @Override
    public void start() {
        getContainers().start();
    }
    
    @Override
    public void stop() {
        getContainers().stop();
    }
}
