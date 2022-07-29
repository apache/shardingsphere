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
 * MySQL container.
 */
public final class MySQLContainer extends DockerStorageContainer {
    
    public MySQLContainer(final String dockerImageName, final String scenario) {
        super(DatabaseTypeFactory.getInstance("MySQL"), Strings.isNullOrEmpty(dockerImageName) ? "mysql/mysql-server:5.7" : dockerImageName, scenario);
    }
    
    @Override
    protected void configure() {
        // TODO need auto set server-id by generator, now always set server-id to 1
        setCommand("--server-id=1");
        addEnv("LANG", "C.UTF-8");
        addEnv("MYSQL_RANDOM_ROOT_PASSWORD", "yes");
        withClasspathResourceMapping("/env/mysql/my.cnf", "/etc/mysql/my.cnf", BindMode.READ_ONLY);
        super.configure();
    }
    
    @Override
    public int getPort() {
        return 3306;
    }
    
    @Override
    protected Optional<String> getDefaultDatabaseName() {
        return Optional.empty();
    }
}
