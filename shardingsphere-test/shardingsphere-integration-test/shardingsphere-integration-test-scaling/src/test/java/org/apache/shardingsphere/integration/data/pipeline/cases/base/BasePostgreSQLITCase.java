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
import org.apache.shardingsphere.integration.data.pipeline.cases.command.ExtraSQLCommand;
import org.apache.shardingsphere.integration.data.pipeline.framework.param.ScalingParameterized;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;

import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public abstract class BasePostgreSQLITCase extends BaseITCase {
    
    private final ExtraSQLCommand extraSQLCommand;
    
    public BasePostgreSQLITCase(final ScalingParameterized parameterized) {
        super(parameterized);
        extraSQLCommand = JAXB.unmarshal(BasePostgreSQLITCase.class.getClassLoader().getResource(parameterized.getScenario()), ExtraSQLCommand.class);
    }
    
    @SneakyThrows(SQLException.class)
    protected void addSourceResource(final String username, final String password) {
        Properties queryProps = createQueryProperties();
        try (Connection connection = DriverManager.getConnection(JDBC_URL_APPENDER.appendQueryProperties(getComposedContainer().getProxyJdbcUrl("sharding_db"), queryProps), "root", "root")) {
            addSourceResource(connection, username, password);
        }
    }
    
    protected void createOrderTable() {
        getJdbcTemplate().execute(extraSQLCommand.getCreateTableOrder());
    }
    
    protected void createOrderItemTable() {
        getJdbcTemplate().execute(extraSQLCommand.getCreateTableOrderItem());
    }
    
    protected void batchInsertOrder(final KeyGenerateAlgorithm keyGenerateAlgorithm) {
        List<Object[]> orderData = new ArrayList<>(3000);
        for (int i = 1; i <= 3000; i++) {
            orderData.add(new Object[]{keyGenerateAlgorithm.generateKey(), ThreadLocalRandom.current().nextInt(0, 6), ThreadLocalRandom.current().nextInt(0, 6), "OK"});
        }
        getJdbcTemplate().batchUpdate("INSERT INTO t_order (id,order_id,user_id,status) VALUES (?, ?, ?, ?)", orderData);
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
