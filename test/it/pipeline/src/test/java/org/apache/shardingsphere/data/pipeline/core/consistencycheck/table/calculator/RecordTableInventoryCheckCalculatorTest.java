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
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.RecordTableInventoryCheckCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableInventoryCheckCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.QueryRange;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.QueryType;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.StreamingRangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.calculator.TableInventoryCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecordTableInventoryCheckCalculatorTest {
    
    private static PipelineDataSource dataSource;
    
    @BeforeAll
    static void setUp() throws Exception {
        dataSource = new PipelineDataSource(createHikariDataSource("calc_" + RandomStringUtils.randomAlphanumeric(9)), TypedSPILoader.getService(DatabaseType.class, "H2"));
        createTableAndInitData(dataSource);
    }
    
    @AfterAll
    static void tearDown() {
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
    
    @ParameterizedTest
    @CsvSource({"SMALL", "LARGE"})
    void assertCalculateOfRangeQueryFromBeginWithOrderIdUniqueKey(final String streamingRangeType) {
        RecordTableInventoryCheckCalculator calculator = new RecordTableInventoryCheckCalculator(4, StreamingRangeType.valueOf(streamingRangeType));
        TableInventoryCalculateParameter param = new TableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "t_order"),
                Collections.emptyList(), buildOrderIdUniqueKey(), QueryType.RANGE_QUERY, null);
        assertQueryRangeCalculatedResult(calculator, param, new QueryRange(0, false, null), 4, 4);
    }
    
    @ParameterizedTest
    @CsvSource({"SMALL", "LARGE"})
    void assertCalculateOfRangeQueryFromMiddleWithOrderIdUniqueKey(final String streamingRangeType) {
        RecordTableInventoryCheckCalculator calculator = new RecordTableInventoryCheckCalculator(4, StreamingRangeType.valueOf(streamingRangeType));
        TableInventoryCalculateParameter param = new TableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "t_order"),
                Collections.emptyList(), buildOrderIdUniqueKey(), QueryType.RANGE_QUERY, null);
        assertQueryRangeCalculatedResult(calculator, param, new QueryRange(4, false, null), 4, 8);
    }
    
    @ParameterizedTest
    @CsvSource({"SMALL", "LARGE"})
    void assertCalculateOfRangeQueryWithMultiColumnUniqueKeys(final String streamingRangeType) {
        RecordTableInventoryCheckCalculator calculator = new RecordTableInventoryCheckCalculator(1000, StreamingRangeType.valueOf(streamingRangeType));
        TableInventoryCalculateParameter param = new TableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "t_order"),
                Collections.emptyList(), buildMultiColumnUniqueKeys(), QueryType.RANGE_QUERY, null);
        assertQueryRangeCalculatedResult(calculator, param, new QueryRange(3, true, 6), 8, 6);
        assertQueryRangeCalculatedResult(calculator, param, new QueryRange(3, false, 6), 3, 6);
    }
    
    private void assertQueryRangeCalculatedResult(final RecordTableInventoryCheckCalculator calculator, final TableInventoryCalculateParameter param, final QueryRange queryRange,
                                                  final int expectedRecordsCount, final int expectedMaxUniqueKeyValue) {
        param.setQueryRange(queryRange);
        Optional<TableInventoryCheckCalculatedResult> calculatedResult = calculator.calculateChunk(param);
        QuietlyCloser.close(param.getCalculationContext());
        assertTrue(calculatedResult.isPresent());
        TableInventoryCheckCalculatedResult actual = calculatedResult.get();
        assertThat(actual.getRecordsCount(), is(expectedRecordsCount));
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(expectedMaxUniqueKeyValue));
    }
    
    @ParameterizedTest
    @CsvSource({"SMALL", "LARGE"})
    void assertCalculateOfRangeQueryWithMultiColumnUniqueKeys2(final String streamingRangeType) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("DROP TABLE IF EXISTS test3");
            connection.createStatement().execute("CREATE TABLE test3 (user_id INT NOT NULL, order_id INT, status VARCHAR(12))");
            connection.createStatement().execute(
                    "INSERT INTO test3 (user_id,order_id,status) VALUES (3,1,'ok'),(3,2,'ok'),(4,3,'ok'),(4,4,'ok'),(5,5,'ok'),(5,6,'ok'),(6,7,'ok'),(6,8,'ok'),(7,9,'ok')");
        }
        RecordTableInventoryCheckCalculator calculator = new RecordTableInventoryCheckCalculator(1000, StreamingRangeType.valueOf(streamingRangeType));
        TableInventoryCalculateParameter param = new TableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "test3"),
                Collections.emptyList(), buildMultiColumnUniqueKeys(), QueryType.RANGE_QUERY, null);
        assertQueryRangeCalculatedResult(calculator, param, new QueryRange(3, true, 4), 4, 4);
        assertQueryRangeCalculatedResult(calculator, param, new QueryRange(5, true, 6), 4, 6);
        assertQueryRangeCalculatedResult(calculator, param, new QueryRange(5, true, 7), 5, 7);
    }
    
    @ParameterizedTest
    @CsvSource({"SMALL", "LARGE"})
    void assertCalculateOfReservedRangeQueryWithMultiColumnUniqueKeys(final String streamingRangeType) {
        RecordTableInventoryCheckCalculator calculator = new RecordTableInventoryCheckCalculator(1000, StreamingRangeType.valueOf(streamingRangeType));
        TableInventoryCalculateParameter param = new TableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "t_order"),
                Collections.emptyList(), buildMultiColumnUniqueKeys(), QueryType.RANGE_QUERY, null);
        param.setQueryRange(new QueryRange(3, true, 2));
        Optional<TableInventoryCheckCalculatedResult> calculatedResult = calculator.calculateChunk(param);
        QuietlyCloser.close(param.getCalculationContext());
        assertFalse(calculatedResult.isPresent());
    }
    
    @ParameterizedTest
    @CsvSource({"SMALL", "LARGE"})
    void assertCalculateOfRangeQueryOnEmptyTableWithSingleColumnUniqueKey(final String streamingRangeType) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("DROP TABLE IF EXISTS test1");
            connection.createStatement().execute("CREATE TABLE test1 (user_id INT NOT NULL, order_id INT, status VARCHAR(12), PRIMARY KEY (user_id))");
        }
        TableInventoryCalculateParameter param = new TableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "test1"),
                Collections.emptyList(), buildUserIdUniqueKey(), QueryType.RANGE_QUERY, null);
        param.setQueryRange(new QueryRange(0, false, null));
        RecordTableInventoryCheckCalculator calculator = new RecordTableInventoryCheckCalculator(5, StreamingRangeType.valueOf(streamingRangeType));
        Optional<TableInventoryCheckCalculatedResult> calculateResult = calculator.calculateChunk(param);
        QuietlyCloser.close(param.getCalculationContext());
        assertFalse(calculateResult.isPresent());
    }
    
    @ParameterizedTest
    @CsvSource({"SMALL", "LARGE"})
    void assertCalculateOfRangeQueryOnEmptyTableWithMultiColumnUniqueKeys(final String streamingRangeType) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("DROP TABLE IF EXISTS test2");
            connection.createStatement().execute("CREATE TABLE test2 (user_id INT NOT NULL, order_id INT, status VARCHAR(12), PRIMARY KEY (user_id, order_id))");
        }
        TableInventoryCalculateParameter param = new TableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "test2"),
                Collections.emptyList(), buildMultiColumnUniqueKeys(), QueryType.RANGE_QUERY, null);
        param.setQueryRange(new QueryRange(null, false, null));
        RecordTableInventoryCheckCalculator calculator = new RecordTableInventoryCheckCalculator(5, StreamingRangeType.valueOf(streamingRangeType));
        Optional<TableInventoryCheckCalculatedResult> calculateResult = calculator.calculateChunk(param);
        QuietlyCloser.close(param.getCalculationContext());
        assertFalse(calculateResult.isPresent());
    }
    
    @ParameterizedTest
    @CsvSource({"100,SMALL", "2,SMALL", "3,SMALL", "100,LARGE", "2,LARGE", "3,LARGE"})
    void assertCalculateOfRangeQueryAllWithOrderIdUniqueKeyWith3x(final int streamingChunkCount, final String streamingRangeType) {
        RecordTableInventoryCheckCalculator calculator = new RecordTableInventoryCheckCalculator(3, streamingChunkCount, StreamingRangeType.valueOf(streamingRangeType));
        TableInventoryCalculateParameter param = new TableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "t_order"),
                Collections.emptyList(), buildOrderIdUniqueKey(), QueryType.RANGE_QUERY, null);
        param.setQueryRange(new QueryRange(null, false, null));
        Iterator<TableInventoryCheckCalculatedResult> resultIterator = calculator.calculate(param).iterator();
        RecordTableInventoryCheckCalculatedResult actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(3));
        assertRecord(actual.getRecords().get(0), 1, 1);
        assertRecord(actual.getRecords().get(1), 2, 2);
        assertRecord(actual.getRecords().get(2), 3, 3);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(3));
        actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(3));
        assertRecord(actual.getRecords().get(0), 3, 4);
        assertRecord(actual.getRecords().get(1), 3, 5);
        assertRecord(actual.getRecords().get(2), 3, 6);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(6));
        actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(3));
        assertRecord(actual.getRecords().get(0), 3, 7);
        assertRecord(actual.getRecords().get(1), 4, 8);
        assertRecord(actual.getRecords().get(2), 5, 9);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(9));
        actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(1));
        assertRecord(actual.getRecords().get(0), 6, 10);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(10));
        assertFalse(resultIterator.hasNext());
        QuietlyCloser.close(param.getCalculationContext());
    }
    
    @ParameterizedTest
    @CsvSource({"SMALL", "LARGE"})
    void assertCalculateOfRangeQueryAllWithMultiColumnUniqueKeysWith50x100(final String streamingRangeType) {
        RecordTableInventoryCheckCalculator calculator = new RecordTableInventoryCheckCalculator(50, 100, StreamingRangeType.valueOf(streamingRangeType));
        TableInventoryCalculateParameter param = new TableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "t_order"),
                Collections.emptyList(), buildMultiColumnUniqueKeys(), QueryType.RANGE_QUERY, null);
        param.setQueryRange(new QueryRange(null, false, null));
        Iterator<TableInventoryCheckCalculatedResult> resultIterator = calculator.calculate(param).iterator();
        RecordTableInventoryCheckCalculatedResult actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(10));
        assertRecord(actual.getRecords().get(0), 1, 1);
        assertRecord(actual.getRecords().get(1), 2, 2);
        assertRecord(actual.getRecords().get(2), 3, 3);
        assertRecord(actual.getRecords().get(3), 3, 4);
        assertRecord(actual.getRecords().get(4), 3, 5);
        assertRecord(actual.getRecords().get(5), 3, 6);
        assertRecord(actual.getRecords().get(6), 3, 7);
        assertRecord(actual.getRecords().get(7), 4, 8);
        assertRecord(actual.getRecords().get(8), 5, 9);
        assertRecord(actual.getRecords().get(9), 6, 10);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(6));
        assertFalse(resultIterator.hasNext());
        QuietlyCloser.close(param.getCalculationContext());
    }
    
    @ParameterizedTest
    @CsvSource({"100,SMALL", "2,SMALL", "3,SMALL", "100,LARGE", "2,LARGE", "3,LARGE"})
    void assertCalculateOfRangeQueryAllWithMultiColumnUniqueKeysWith3x(final int streamingChunkCount, final String streamingRangeType) {
        RecordTableInventoryCheckCalculator calculator = new RecordTableInventoryCheckCalculator(3, streamingChunkCount, StreamingRangeType.valueOf(streamingRangeType));
        TableInventoryCalculateParameter param = new TableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "t_order"),
                Collections.emptyList(), buildMultiColumnUniqueKeys(), QueryType.RANGE_QUERY, null);
        param.setQueryRange(new QueryRange(null, false, null));
        Iterator<TableInventoryCheckCalculatedResult> resultIterator = calculator.calculate(param).iterator();
        RecordTableInventoryCheckCalculatedResult actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(3));
        assertRecord(actual.getRecords().get(0), 1, 1);
        assertRecord(actual.getRecords().get(1), 2, 2);
        assertRecord(actual.getRecords().get(2), 3, 3);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(3));
        actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(3));
        assertRecord(actual.getRecords().get(0), 3, 4);
        assertRecord(actual.getRecords().get(1), 3, 5);
        assertRecord(actual.getRecords().get(2), 3, 6);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(3));
        actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(3));
        assertRecord(actual.getRecords().get(0), 3, 7);
        assertRecord(actual.getRecords().get(1), 4, 8);
        assertRecord(actual.getRecords().get(2), 5, 9);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(5));
        actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(1));
        assertRecord(actual.getRecords().get(0), 6, 10);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(6));
        assertFalse(resultIterator.hasNext());
        QuietlyCloser.close(param.getCalculationContext());
    }
    
    @ParameterizedTest
    @CsvSource({"100,SMALL", "3,SMALL", "4,SMALL", "100,LARGE", "3,LARGE", "4,LARGE"})
    void assertCalculateOfRangeQueryAllWithMultiColumnUniqueKeysWith2x(final int streamingChunkCount, final String streamingRangeType) {
        RecordTableInventoryCheckCalculator calculator = new RecordTableInventoryCheckCalculator(2, streamingChunkCount, StreamingRangeType.valueOf(streamingRangeType));
        TableInventoryCalculateParameter param = new TableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "t_order"),
                Collections.emptyList(), buildMultiColumnUniqueKeys(), QueryType.RANGE_QUERY, null);
        param.setQueryRange(new QueryRange(null, false, null));
        Iterator<TableInventoryCheckCalculatedResult> resultIterator = calculator.calculate(param).iterator();
        RecordTableInventoryCheckCalculatedResult actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(2));
        assertRecord(actual.getRecords().get(0), 1, 1);
        assertRecord(actual.getRecords().get(1), 2, 2);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(2));
        actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(2));
        assertRecord(actual.getRecords().get(0), 3, 3);
        assertRecord(actual.getRecords().get(1), 3, 4);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(3));
        actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(2));
        assertRecord(actual.getRecords().get(0), 3, 5);
        assertRecord(actual.getRecords().get(1), 3, 6);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(3));
        actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(2));
        assertRecord(actual.getRecords().get(0), 3, 7);
        assertRecord(actual.getRecords().get(1), 4, 8);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(4));
        actual = (RecordTableInventoryCheckCalculatedResult) resultIterator.next();
        assertThat(actual.getRecordsCount(), is(2));
        assertRecord(actual.getRecords().get(0), 5, 9);
        assertRecord(actual.getRecords().get(1), 6, 10);
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(6));
        assertFalse(resultIterator.hasNext());
        QuietlyCloser.close(param.getCalculationContext());
    }
    
    private void assertRecord(final Map<String, Object> recordMap, final int userId, final int orderId) {
        assertThat(recordMap.get("user_id"), is(userId));
        assertThat(recordMap.get("order_id"), is(orderId));
    }
    
    @ParameterizedTest
    @CsvSource({"SMALL", "LARGE"})
    void assertCalculateOfPointQuery(final String streamingRangeType) {
        RecordTableInventoryCheckCalculator calculator = new RecordTableInventoryCheckCalculator(3, StreamingRangeType.valueOf(streamingRangeType));
        TableInventoryCalculateParameter param = new TableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "t_order"),
                Collections.emptyList(), buildMultiColumnUniqueKeys(), QueryType.POINT_QUERY, null);
        param.setUniqueKeysValues(Arrays.asList(3, 3));
        Optional<TableInventoryCheckCalculatedResult> calculatedResult = calculator.calculateChunk(param);
        QuietlyCloser.close(param.getCalculationContext());
        assertTrue(calculatedResult.isPresent());
        TableInventoryCheckCalculatedResult actual = calculatedResult.get();
        assertThat(actual.getRecordsCount(), is(1));
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(3));
    }
    
    @ParameterizedTest
    @CsvSource({"SMALL", "LARGE"})
    void assertCalculateOfPointRangeQuery(final String streamingRangeType) {
        RecordTableInventoryCheckCalculator calculator = new RecordTableInventoryCheckCalculator(3, StreamingRangeType.valueOf(streamingRangeType));
        TableInventoryCalculateParameter param = new TableInventoryCalculateParameter(dataSource, new QualifiedTable(null, "t_order"),
                Collections.emptyList(), buildUserIdUniqueKey(), QueryType.POINT_QUERY, null);
        param.setUniqueKeysValues(Collections.singleton(3));
        Optional<TableInventoryCheckCalculatedResult> calculatedResult = calculator.calculateChunk(param);
        QuietlyCloser.close(param.getCalculationContext());
        assertTrue(calculatedResult.isPresent());
        TableInventoryCheckCalculatedResult actual = calculatedResult.get();
        assertThat(actual.getRecordsCount(), is(5));
        assertTrue(actual.getMaxUniqueKeyValue().isPresent());
        assertThat(actual.getMaxUniqueKeyValue().get(), is(3));
    }
    
    private List<PipelineColumnMetaData> buildUserIdUniqueKey() {
        return Collections.singletonList(new PipelineColumnMetaData(1, "user_id", Types.INTEGER, "integer", false, true, true));
    }
    
    private List<PipelineColumnMetaData> buildOrderIdUniqueKey() {
        return Collections.singletonList(new PipelineColumnMetaData(1, "order_id", Types.INTEGER, "integer", false, true, true));
    }
    
    private List<PipelineColumnMetaData> buildMultiColumnUniqueKeys() {
        return Arrays.asList(
                new PipelineColumnMetaData(1, "user_id", Types.INTEGER, "integer", false, true, true),
                new PipelineColumnMetaData(2, "order_id", Types.INTEGER, "integer", false, true, true));
    }
}
