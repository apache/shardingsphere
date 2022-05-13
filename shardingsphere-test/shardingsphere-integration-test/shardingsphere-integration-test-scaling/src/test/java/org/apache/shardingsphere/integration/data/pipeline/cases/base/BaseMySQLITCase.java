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

package org.apache.shardingsphere.integration.data.pipeline.cases.base;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.BaseITCase;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.ExtraSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.cases.mysql.MySQLIncrementTaskRunnable;
import org.apache.shardingsphere.integration.data.pipeline.framework.helper.SQLHelper;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public abstract class BaseMySQLITCase extends BaseITCase {
    
    protected static final DatabaseType DATABASE_TYPE = new MySQLDatabaseType();
    
    private final ExtraSQLCommand extraSQLCommand;
    
    @Getter
    private final SQLHelper sqlHelper;
    
    public BaseMySQLITCase(final ScalingParameterized parameterized) {
        super(parameterized);
        extraSQLCommand = JAXB.unmarshal(BaseMySQLITCase.class.getClassLoader().getResource(parameterized.getScenario()), ExtraSQLCommand.class);
        sqlHelper = new SQLHelper(DATABASE_TYPE, extraSQLCommand, getJdbcTemplate());
    }
    
    @SneakyThrows(SQLException.class)
    protected void addResource() {
        Properties queryProps = createQueryProperties();
        // TODO if use jdbcurl like "jdbc:mysql:localhost:3307/sharding_db", will throw exception show "Datasource or ShardingSphere rule does not exist"
        try (Connection connection = DriverManager.getConnection(JDBC_URL_APPENDER.appendQueryProperties(getComposedContainer().getProxyJdbcUrl(""), queryProps), "root", "root")) {
            connection.createStatement().execute("USE sharding_db");
            addResource(connection);
        }
    }
    
    protected void startIncrementTask(final KeyGenerateAlgorithm keyGenerateAlgorithm) {
        setIncreaseTaskThread(new Thread(new MySQLIncrementTaskRunnable(getJdbcTemplate(), extraSQLCommand, keyGenerateAlgorithm)));
        getIncreaseTaskThread().start();
    }
    
    @Override
    protected Properties createQueryProperties() {
        Properties result = new Properties();
        result.put("useSSL", Boolean.FALSE.toString());
        result.put("rewriteBatchedStatements", Boolean.TRUE.toString());
        result.put("serverTimezone", "UTC");
        return result;
    }
}
