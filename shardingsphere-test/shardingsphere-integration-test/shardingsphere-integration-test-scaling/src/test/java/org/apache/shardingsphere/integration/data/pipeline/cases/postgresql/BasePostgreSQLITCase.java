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

package org.apache.shardingsphere.integration.data.pipeline.cases.postgresql;

import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipeline.cases.BaseITCase;
import org.apache.shardingsphere.integration.data.pipeline.cases.command.ExtraSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.integration.data.pipeline.util.TableCrudUtil;

import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public abstract class BasePostgreSQLITCase extends BaseITCase {
    
    protected static final DatabaseType DATABASE = new PostgreSQLDatabaseType();
    
    private final ExtraSQLCommand extraSQLCommand;
    
    public BasePostgreSQLITCase(final ScalingParameterized parameterized) {
        super(parameterized);
        extraSQLCommand = JAXB.unmarshal(Objects.requireNonNull(BasePostgreSQLITCase.class.getClassLoader().getResource(parameterized.getParentPath() + "/sql.xml")), ExtraSQLCommand.class);
        initTableAndData();
    }
    
    @SneakyThrows({SQLException.class, InterruptedException.class})
    protected void initTableAndData() {
        Properties queryProps = createQueryProperties();
        try (Connection connection = DriverManager.getConnection(JDBC_URL_APPENDER.appendQueryProperties(getComposedContainer().getProxyJdbcUrl("sharding_db"), queryProps), "root", "root")) {
            addResource(connection);
        }
        initShardingRule();
        setIncreaseTaskThread(new Thread(new PostgreSQLIncrementTaskRunnable(getJdbcTemplate(), extraSQLCommand)));
        getJdbcTemplate().execute(extraSQLCommand.getCreateTableOrder());
        getJdbcTemplate().execute(extraSQLCommand.getCreateTableOrderItem());
        getIncreaseTaskThread().start();
        Pair<List<Object[]>, List<Object[]>> dataPair = TableCrudUtil.generatePostgresSQLInsertDataList(3000);
        getJdbcTemplate().batchUpdate(extraSQLCommand.getFullInsertOrder(), dataPair.getLeft());
        getJdbcTemplate().batchUpdate(extraSQLCommand.getInsertOrderItem(), dataPair.getRight());
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
