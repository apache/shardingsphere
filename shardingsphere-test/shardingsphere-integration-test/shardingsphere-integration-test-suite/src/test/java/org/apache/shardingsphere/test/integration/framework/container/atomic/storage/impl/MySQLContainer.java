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

package org.apache.shardingsphere.test.integration.framework.container.atomic.storage.impl;

import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.framework.container.atomic.storage.DockerStorageContainer;

import java.util.Collections;

/**
 * MySQL container.
 */
public final class MySQLContainer extends DockerStorageContainer {
    
    public MySQLContainer(final String scenario) {
        super(DatabaseTypeRegistry.getActualDatabaseType("MySQL"), "mysql/mysql-server:5.7", scenario);
    }
    
    @Override
    protected void configure() {
        withCommand("--sql_mode=", "--default-authentication-plugin=mysql_native_password");
        setEnv(Collections.singletonList("LANG=C.UTF-8"));
        super.configure();
    }
    
    @Override
    protected int getPort() {
        return 3306;
    }
}
