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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.RecordSingleTableInventoryCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.SingleTableInventoryCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.QueryType;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.range.QueryRange;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordSingleTableInventoryCalculatorTest {
    
    private static PipelineDataSource dataSource;
    
    @BeforeAll
    static void setUp() throws Exception {
        dataSource = new PipelineDataSource(createHikariDataSource("calc_" + RandomStringUtils.randomAlphanumeric(9)), TypedSPILoader.getService(DatabaseType.class, "H2"));
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
        result.setConnectionTimeout(15L * 1000L);
        result.setIdleTimeout(40L * 1000L);
        return result;
    }
    
    private static void createTableAndInitData(final PipelineDataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "CREATE TABLE t_order (user_id INT NOT NULL, order_id INT, status VARCHAR(12), PRIMARY KEY (user_id, order_id))";
            connection.createStatement().execute(sql);
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_order (user_id, order_id, status) VALUES (?, ?, ?)");
            insertRecord(preparedStatement, 1, 1);
            insertRecord(preparedStatement, 2, 2);
            insertRecord(preparedStatement, 3, 3);
            insertRecord(preparedStatement, 3, 4);
            insertRecord(preparedStatement, 3, 5);
            insertRecord(preparedStatement, 3, 6);
            insertRecord(preparedStatement, 3, 7);
            insertRecord(preparedStatement, 4, 8);
            insertRecord(preparedStatement, 5, 9);
            insertRecord(preparedStatement, 6, 10);
        }
    }
    
    private static void insertRecord(final PreparedStatement preparedStatement, final int userId, final int orderId) throws SQLException {
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, orderId);
        preparedStatement.setString(3, "OK");
        preparedStatement.executeUpdate();
    }
    
    @Test
    void assertCalculateOfAllQueryFromBegin() {
        RecordSingleTableInventoryCalculator calculator = new RecordSingleTableInventoryCalculator(5);
        SingleTableInventoryCalculateParameter param = generateParameter(dataSource, 0);
        Optional<SingleTableInventoryCalculatedResult> calculateResult = calculator.calculateChunk(param);
        assertTrue(calculateResult.isPresent());
        SingleTableInventoryCalculatedResult actual = calculateResult.get();
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(4));
    }
    
    @Test
    void assertCalculateOfAllQueryFromMiddle() {
        RecordSingleTableInventoryCalculator calculator = new RecordSingleTableInventoryCalculator(5);
        SingleTableInventoryCalculateParameter param = generateParameter(dataSource, 5);
        Optional<SingleTableInventoryCalculatedResult> calculateResult = calculator.calculateChunk(param);
        assertTrue(calculateResult.isPresent());
        SingleTableInventoryCalculatedResult actual = calculateResult.get();
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(9));
    }
    
    private SingleTableInventoryCalculateParameter generateParameter(final PipelineDataSource dataSource, final Object dataCheckPosition) {
        List<PipelineColumnMetaData> uniqueKeys = Collections.singletonList(new PipelineColumnMetaData(1, "order_id", Types.INTEGER, "integer", false, true, true));
        return new SingleTableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "t_order"), Collections.emptyList(), uniqueKeys, dataCheckPosition);
    }
    
    @Test
    void assertCalculateOfRangeQuery() {
        RecordSingleTableInventoryCalculator calculator = new RecordSingleTableInventoryCalculator(1000);
        SingleTableInventoryCalculateParameter param = new SingleTableInventoryCalculateParameter(
                dataSource, new QualifiedTable(null, "t_order"), Collections.emptyList(), buildUniqueKeys(), QueryType.RANGE_QUERY);
        param.setQueryRange(new QueryRange(3, true, 7));
        Optional<SingleTableInventoryCalculatedResult> calculatedResult = calculator.calculateChunk(param);
        assertTrue(calculatedResult.isPresent());
        SingleTableInventoryCalculatedResult actual = calculatedResult.get();
        assertThat(actual.getRecordsCount(), is(8));
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(6));
    }
    
    @Test
    void assertCalculateOfRangeQueryAll() {
        RecordSingleTableInventoryCalculator calculator = new RecordSingleTableInventoryCalculator(3);
        SingleTableInventoryCalculateParameter param = new SingleTableInventoryCalculateParameter(dataSource,
                new QualifiedTable(null, "t_order"), Collections.emptyList(), buildUniqueKeys(), QueryType.RANGE_QUERY);
        param.setQueryRange(new QueryRange(null, false, null));
        Iterator<SingleTableInventoryCalculatedResult> resultIterator = calculator.calculate(param).iterator();
        RecordSingleTableInventoryCalculatedResult actual = (RecordSingleTableInventoryCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(2));
        assertRecord(actual.getRecords().get(0), 1, 1);
        assertRecord(actual.getRecords().get(1), 2, 2);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(2));
        actual = (RecordSingleTableInventoryCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(5));
        assertRecord(actual.getRecords().get(0), 3, 3);
        assertRecord(actual.getRecords().get(1), 3, 4);
        assertRecord(actual.getRecords().get(2), 3, 5);
        assertRecord(actual.getRecords().get(3), 3, 6);
        assertRecord(actual.getRecords().get(4), 3, 7);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(3));
        actual = (RecordSingleTableInventoryCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(2));
        assertRecord(actual.getRecords().get(0), 4, 8);
        assertRecord(actual.getRecords().get(1), 5, 9);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(5));
        actual = (RecordSingleTableInventoryCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(1));
        assertRecord(actual.getRecords().get(0), 6, 10);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(6));
    }
    
    private void assertRecord(final Map<String, Object> recordMap, final int userId, final int orderId) {
        assertThat(recordMap.get("user_id"), is(userId));
        assertThat(recordMap.get("order_id"), is(orderId));
    }
    
    @Test
    void assertCalculateOfPointQuery() {
        RecordSingleTableInventoryCalculator calculator = new RecordSingleTableInventoryCalculator(3);
        SingleTableInventoryCalculateParameter param = new SingleTableInventoryCalculateParameter(dataSource,
                new QualifiedTable(null, "t_order"), Collections.emptyList(), buildUniqueKeys(), QueryType.POINT_QUERY);
        param.setUniqueKeysValues(Arrays.asList(3, 3));
        Optional<SingleTableInventoryCalculatedResult> calculatedResult = calculator.calculateChunk(param);
        assertTrue(calculatedResult.isPresent());
        SingleTableInventoryCalculatedResult actual = calculatedResult.get();
        assertThat(actual.getRecordsCount(), is(1));
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(3));
    }
    
    private List<PipelineColumnMetaData> buildUniqueKeys() {
        return Arrays.asList(
                new PipelineColumnMetaData(1, "user_id", Types.INTEGER, "integer", false, true, true),
                new PipelineColumnMetaData(2, "order_id", Types.INTEGER, "integer", false, true, true));
    }
}
