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

package org.apache.shardingsphere.integration.data.pipeline.container.database;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.integration.data.pipeline.env.enums.ITEnvTypeEnum;
import org.apache.shardingsphere.test.integration.env.DataSourceEnvironment;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;

import java.util.Properties;

public final class MySQLContainer extends DockerDatabaseContainer {
    
    private static final DatabaseType DATABASE_TYPE = new MySQLDatabaseType();
    
    public MySQLContainer(final String dockerImageName) {
        super(DATABASE_TYPE, dockerImageName);
    }
    
    @Override
    protected void configure() {
        withCommand("--sql_mode=", "--default-authentication-plugin=mysql_native_password");
        setEnv(Lists.newArrayList("LANG=C.UTF-8", "MYSQL_ROOT_PASSWORD=root", "MYSQL_ROOT_HOST=%"));
        withClasspathResourceMapping("/env/mysql/my.cnf", "/etc/mysql/my.cnf", BindMode.READ_ONLY);
        super.configure();
        withExposedPorts(getPort());
        setWaitStrategy(new LogMessageWaitStrategy().withRegEx(".*ready for connections.*"));
        if (IntegrationTestEnvironment.getInstance().getItEnvType() == ITEnvTypeEnum.LOCAL) {
            addFixedExposedPort(3306, 3306);
        }
    }
    
    @Override
    public String getJdbcUrl(final String host, final int port, final String databaseName) {
        String jdbcUrl = DataSourceEnvironment.getURL(DATABASE_TYPE, host, port, databaseName);
        return new JdbcUrlAppender().appendQueryProperties(jdbcUrl, createQueryProperties());
    }
    
    private Properties createQueryProperties() {
        Properties result = new Properties();
        result.put("useSSL", Boolean.FALSE.toString());
        result.put("rewriteBatchedStatements", Boolean.TRUE.toString());
        return result;
    }
    
    @Override
    public int getPort() {
        return 3306;
    }
}
