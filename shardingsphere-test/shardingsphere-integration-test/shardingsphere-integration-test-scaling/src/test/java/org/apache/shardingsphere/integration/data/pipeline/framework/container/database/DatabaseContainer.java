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

/**
 * Docker storage container.
 */
@Getter
public abstract class DatabaseContainer extends DockerITContainer {
    
    private final DatabaseType databaseType;
    
    public DatabaseContainer(final DatabaseType databaseType, final String dockerImageName) {
        super(databaseType.getType().toLowerCase(), dockerImageName);
        this.databaseType = databaseType;
    }
    
    /**
     * Get jdbc url.
     *
     * @param databaseName database name
     * @return jdbc url
     */
    public abstract String getJdbcUrl(String databaseName);
    
    /**
     * Get database username.
     *
     * @return database username
     */
    public abstract String getUsername();
    
    /**
     * Get database password.
     *
     * @return database username
     */
    public abstract String getPassword();
    
    /**
     * Get database port.
     *
     * @return database port
     */
    public abstract int getPort();
}
