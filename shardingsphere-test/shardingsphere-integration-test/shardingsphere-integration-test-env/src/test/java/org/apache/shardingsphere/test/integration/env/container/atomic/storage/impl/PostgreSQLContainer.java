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
import org.apache.shardingsphere.test.integration.env.container.atomic.util.CommandPartUtil;
import org.testcontainers.containers.BindMode;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL container.
 */
public final class PostgreSQLContainer extends DockerStorageContainer {
    
    private static final String[] DEFAULT_COMMAND_PARTS = new String[]{"config_file=/etc/postgresql/postgresql.conf"};
    
    private final String[] commandParts;
    
    public PostgreSQLContainer(final String dockerImageName, final String scenario, final boolean useRootUsername, final String... commandParts) {
        super(DatabaseTypeFactory.getInstance("PostgreSQL"), Strings.isNullOrEmpty(dockerImageName) ? "postgres:12-alpine" : dockerImageName, scenario, useRootUsername);
        this.commandParts = commandParts;
    }
    
    @Override
    protected void configure() {
        List<String> commandParts = new LinkedList<>();
        for (String each : CommandPartUtil.mergeCommandParts(DEFAULT_COMMAND_PARTS, this.commandParts)) {
            commandParts.add("-c");
            commandParts.add(each);
        }
        setCommand(commandParts.toArray(new String[0]));
        addEnv("POSTGRES_USER", getRootUsername());
        addEnv("POSTGRES_PASSWORD", getUnifiedPassword());
        withClasspathResourceMapping("/env/postgresql/postgresql.conf", "/etc/postgresql/postgresql.conf", BindMode.READ_ONLY);
        super.configure();
    }
    
    @Override
    public String getRootUsername() {
        return "root";
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
