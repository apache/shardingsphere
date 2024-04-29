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

package org.apache.shardingsphere.test.it.data.pipeline.core.consistencycheck.table.calculator;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shardingsphere.infra.metadata.caseinsensitive.CaseInsensitiveQualifiedTable;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.SingleTableInventoryCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator.RecordSingleTableInventoryCalculator;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator.SingleTableInventoryCalculateParameter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordSingleTableInventoryCalculatorTest {
    
    private static PipelineDataSourceWrapper dataSource;
    
    @BeforeAll
    static void setUp() throws Exception {
        dataSource = new PipelineDataSourceWrapper(createHikariDataSource("calc_" + RandomStringUtils.randomAlphanumeric(9)), TypedSPILoader.getService(DatabaseType.class, "H2"));
        createTableAndInitData(dataSource);
    }
    
    @AfterAll
    static void tearDown() throws Exception {
        dataSource.close();
    }
    
    private static HikariDataSource createHikariDataSource(final String databaseName) {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl(String.format("jdbc:h2:mem:%s;DATABASE_TO_UPPER=false;MODE=MySQL", databaseName));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(10);
        result.setMinimumIdle(2);
        result.setConnectionTimeout(15 * 1000);
        result.setIdleTimeout(40 * 1000);
        return result;
    }
    
    private static void createTableAndInitData(final PipelineDataSourceWrapper dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT NOT NULL, status VARCHAR(12))";
            connection.createStatement().execute(sql);
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_order (order_id, user_id, status) VALUES (?, ?, ?)");
            for (int i = 0; i < 10; i++) {
                preparedStatement.setInt(1, i + 1);
                preparedStatement.setInt(2, i + 1);
                preparedStatement.setString(3, "test");
                preparedStatement.execute();
            }
        }
    }
    
    @Test
    void assertCalculateOfAllQueryFromBegin() {
        RecordSingleTableInventoryCalculator calculator = new RecordSingleTableInventoryCalculator(5);
        SingleTableInventoryCalculateParameter param = generateParameter(dataSource, 0);
        Optional<SingleTableInventoryCalculatedResult> calculateResult = calculator.calculateChunk(param);
        assertTrue(calculateResult.isPresent());
        SingleTableInventoryCalculatedResult actual = calculateResult.get();
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(5));
    }
    
    @Test
    void assertCalculateOfAllQueryFromMiddle() {
        RecordSingleTableInventoryCalculator calculator = new RecordSingleTableInventoryCalculator(5);
        SingleTableInventoryCalculateParameter param = generateParameter(dataSource, 5);
        Optional<SingleTableInventoryCalculatedResult> calculateResult = calculator.calculateChunk(param);
        assertTrue(calculateResult.isPresent());
        SingleTableInventoryCalculatedResult actual = calculateResult.get();
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(10));
    }
    
    private SingleTableInventoryCalculateParameter generateParameter(final PipelineDataSourceWrapper dataSource, final Object dataCheckPosition) {
        List<PipelineColumnMetaData> uniqueKeys = Collections.singletonList(new PipelineColumnMetaData(1, "order_id", Types.INTEGER, "integer", false, true, true));
        return new SingleTableInventoryCalculateParameter(dataSource, new CaseInsensitiveQualifiedTable(null, "t_order"), Collections.emptyList(), uniqueKeys, dataCheckPosition);
    }
}
