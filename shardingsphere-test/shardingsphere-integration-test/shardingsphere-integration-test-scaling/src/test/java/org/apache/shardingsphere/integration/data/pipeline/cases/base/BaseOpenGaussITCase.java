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
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.ExtraSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.cases.common.PostgreSQLIncrementTaskRunnable;
import org.apache.shardingsphere.integration.data.pipeline.framework.helper.ScalingTableSQLHelper;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public abstract class BaseOpenGaussITCase extends BaseITCase {
    
    protected static final DatabaseType DATABASE_TYPE = new OpenGaussDatabaseType();
    
    @Getter
    private final ExtraSQLCommand extraSQLCommand;
    
    @Getter
    private final ScalingTableSQLHelper sqlHelper;
    
    public BaseOpenGaussITCase(final ScalingParameterized parameterized) {
        super(parameterized);
        extraSQLCommand = JAXB.unmarshal(BaseOpenGaussITCase.class.getClassLoader().getResource(parameterized.getScenario()), ExtraSQLCommand.class);
        sqlHelper = new ScalingTableSQLHelper(DATABASE_TYPE, extraSQLCommand, getJdbcTemplate());
    }
    
    @SneakyThrows(SQLException.class)
    protected void addResource() {
        Properties queryProps = createQueryProperties();
        try (Connection connection = DriverManager.getConnection(JDBC_URL_APPENDER.appendQueryProperties(getComposedContainer().getProxyJdbcUrl("sharding_db"), queryProps), "root", "root")) {
            addResource(connection, "gaussdb", "Root@123");
        }
    }
    
    protected void startIncrementTask(final KeyGenerateAlgorithm keyGenerateAlgorithm) {
        setIncreaseTaskThread(new Thread(new PostgreSQLIncrementTaskRunnable(getJdbcTemplate(), extraSQLCommand, keyGenerateAlgorithm)));
        getIncreaseTaskThread().start();
    }
    
    @Override
    protected Properties createQueryProperties() {
        Properties result = new Properties();
        result.put("useSSL", Boolean.FALSE.toString());
        result.put("serverTimezone", "UTC");
        result.put("preferQueryMode", "extendedForPrepared");
        return result;
    }
}
