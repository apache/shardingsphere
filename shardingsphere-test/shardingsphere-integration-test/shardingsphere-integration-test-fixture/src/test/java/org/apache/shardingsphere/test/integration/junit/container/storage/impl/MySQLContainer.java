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

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;

import java.util.Optional;

/**
 * MySQL Container.
 */
public final class MySQLContainer extends ShardingSphereStorageContainer {
    
    public MySQLContainer(final ParameterizedArray parameterizedArray) {
        super("mysql-server", "mysql/mysql-server:5.7", new MySQLDatabaseType(), false, parameterizedArray);
    }
    
    @Override
    protected void configure() {
        withCommand("--sql_mode=", "--default-authentication-plugin=mysql_native_password");
        withInitSQLMapping("/env/" + getParameterizedArray().getScenario() + "/init-sql/mysql");
        setEnv(Lists.newArrayList("LANG=C.UTF-8"));
    }
    
    @Override
    @SneakyThrows
    protected void execute() {
    }
    
    @Override
    protected String getUrl(final String dataSourceName) {
        return "jdbc:mysql://localhost:" + getPort() + "/" + dataSourceName + "?useServerPrepStmts=true&serverTimezone=UTC&useSSL=false&useLocalSessionState=true&characterEncoding=utf-8";
    }
    
    @Override
    protected Optional<String> getConnectionInitSQL() {
        return Optional.of("SET sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");
    }
    
    @Override
    protected int getPort() {
        return getMappedPort(3306);
    }
    
    @Override
    protected String getUsername() {
        return "root";
    }
    
    @Override
    protected String getPassword() {
        return "";
    }
}
