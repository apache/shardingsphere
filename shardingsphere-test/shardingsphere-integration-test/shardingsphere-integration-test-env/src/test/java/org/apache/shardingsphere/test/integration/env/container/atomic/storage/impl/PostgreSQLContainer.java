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

package org.apache.shardingsphere.test.integration.env.container.atomic.storage.impl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.test.integration.env.container.atomic.storage.DockerStorageContainer;
import org.testcontainers.containers.BindMode;

import java.util.Optional;

/**
 * PostgreSQL container.
 */
public final class PostgreSQLContainer extends DockerStorageContainer {
    
    private final String module;
    
    public PostgreSQLContainer(final String dockerImageName, final String scenario, final String module) {
        super(DatabaseTypeFactory.getInstance("PostgreSQL"), Strings.isNullOrEmpty(dockerImageName) ? "postgres:12-alpine" : dockerImageName, scenario, module);
        this.module = module;
    }
    
    @Override
    protected void configure() {
        addEnv("POSTGRES_PASSWORD", getUnifiedPassword());
        if (Strings.isNullOrEmpty(module)) {
            withClasspathResourceMapping("/env/postgresql/postgresql.conf", "/etc/postgresql/postgresql.conf", BindMode.READ_ONLY);
        }
        if ("scaling".equalsIgnoreCase(module)) {
            withClasspathResourceMapping(String.format("/env/%s/postgresql/postgresql.conf", module), "/etc/postgresql/postgresql.conf", BindMode.READ_ONLY);
        }
        setCommand("-c config_file=/etc/postgresql/postgresql.conf");
        super.configure();
    }
    
    @Override
    public int getPort() {
        return 5432;
    }
    
    @Override
    protected Optional<String> getDefaultDatabaseName() {
        return Optional.of("postgres");
    }
}
