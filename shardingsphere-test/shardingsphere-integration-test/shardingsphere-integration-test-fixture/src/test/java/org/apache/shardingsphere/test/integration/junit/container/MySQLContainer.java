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

package org.apache.shardingsphere.test.integration.junit.container;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.testcontainers.containers.BindMode;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.Properties;

/**
 * MySQL Server Container.
 */
public class MySQLContainer extends StorageContainer {
    
    public MySQLContainer() {
        super("mysql/mysql-server:5.7", new MySQLDatabaseType());
    }
    
    public MySQLContainer withInitSQLMapping(final String resourcePath) {
        withClasspathResourceMapping(resourcePath, "/docker-entrypoint-initdb.d/", BindMode.READ_ONLY);
        return this;
    }
    
    @Override
    protected void configure() {
        withInitSQLMapping("/env/db/init-sql/mysql");
        setEnv(Lists.newArrayList("LANG=C.UTF-8"));
    }
    
    @Override
    @SneakyThrows
    protected void execute() {
        execInContainer("--sql_mode=", "--default-authentication-plugin=mysql_native_password");
    }
    
    @Override
    protected DataSource createDataSource(final String dataSourceName) {
        Properties properties = new Properties();
        properties.setProperty("jdbcUrl", getUrl(dataSourceName));
        properties.setProperty("dataSource.user", getUsername());
        properties.setProperty("dataSource.password", getPassword());
        return createHikariCP(properties);
    }
    
    @Override
    protected String getUrl(final String dataSourceName) {
        return "jdbc:mysql://localhost:" + getPort() + "/" + dataSourceName + "?useServerPrepStmts=true&serverTimezone=UTC&useSSL=false&useLocalSessionState=true&characterEncoding=utf-8";
    }
    
    @Override
    protected Optional<String> getConnectionInitSQL() {
        return Optional.of("SET GLOBAL sql_mode=(SELECT REPLACE(@@sql_mode,'ONLY_FULL_GROUP_BY',''))");
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
