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

package org.apache.shardingsphere.test.integration.junit.container.storage.impl;

import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;

/**
 * PostgreSQL container.
 */
public final class PostgreSQLContainer extends ShardingSphereStorageContainer {
    
    public PostgreSQLContainer(final ParameterizedArray parameterizedArray) {
        super("postgres", "postgres:12.6", new PostgreSQLDatabaseType(), false, parameterizedArray);
    }
    
    @Override
    protected void configure() {
        addEnv("POSTGRES_USER", "postgres");
        addEnv("POSTGRES_PASSWORD", "postgres");
        withInitSQLMapping("/env/" + getParameterizedArray().getScenario() + "/init-sql/postgresql");
        super.configure();
    }
    
    @Override
    protected String getUrl(final String dataSourceName) {
        return String.format("jdbc:postgresql://%s:%s/", getHost(), getPort());
    }
    
    @Override
    protected int getPort() {
        return getMappedPort(5432);
    }
    
    @Override
    protected String getUsername() {
        return "postgres";
    }
    
    @Override
    protected String getPassword() {
        return "postgres";
    }
}
