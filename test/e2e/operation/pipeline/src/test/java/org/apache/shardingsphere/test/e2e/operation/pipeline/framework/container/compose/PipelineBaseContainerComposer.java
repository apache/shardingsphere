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

package org.apache.shardingsphere.test.e2e.operation.pipeline.framework.container.compose;

import lombok.Getter;
import org.apache.shardingsphere.test.e2e.env.container.E2EContainers;
import org.testcontainers.lifecycle.Startable;

/**
 * Pipeline base container composer.
 */
@Getter
public abstract class PipelineBaseContainerComposer implements Startable {
    
    private final E2EContainers containers;
    
    protected PipelineBaseContainerComposer() {
        containers = new E2EContainers("");
    }
    
    /**
     * Get proxy jdbc URL.
     *
     * @param databaseName database name
     * @return proxy jdbc URL
     */
    public abstract String getProxyJdbcUrl(String databaseName);
    
    /**
     * Get proxy CDC port.
     *
     * @return proxy port
     */
    public abstract int getProxyCDCPort();
    
    /**
     * Clean up database.
     *
     * @param databaseName database name
     */
    public abstract void cleanUpDatabase(String databaseName);
    
    @Override
    public void start() {
        getContainers().start();
    }
    
    @Override
    public void stop() {
        getContainers().stop();
    }
}
