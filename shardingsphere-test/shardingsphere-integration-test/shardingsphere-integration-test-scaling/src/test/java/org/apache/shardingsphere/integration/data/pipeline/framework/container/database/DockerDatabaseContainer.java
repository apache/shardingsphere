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

package org.apache.shardingsphere.integration.data.pipeline.framework.container.database;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.integration.framework.container.atomic.DockerITContainer;
import org.testcontainers.containers.BindMode;

/**
 * Docker storage container.
 */
@Getter
public abstract class DockerDatabaseContainer extends DockerITContainer {
    
    private final DatabaseType databaseType;
    
    public DockerDatabaseContainer(final DatabaseType databaseType, final String dockerImageName) {
        super(databaseType.getType().toLowerCase(), dockerImageName);
        this.databaseType = databaseType;
    }
    
    @Override
    protected void configure() {
        withClasspathResourceMapping(String.format("/env/%s/initdb.sql", databaseType.getType().toLowerCase()), "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY);
    }
    
    /**
     * Get jdbc url.
     *
     * @param host host
     * @param port port
     * @param databaseName database name
     * @return jdbc url
     */
    public abstract String getJdbcUrl(String host, int port, String databaseName);
    
    /**
     * Get database port.
     *
     * @return database port
     */
    public abstract int getPort();
}
