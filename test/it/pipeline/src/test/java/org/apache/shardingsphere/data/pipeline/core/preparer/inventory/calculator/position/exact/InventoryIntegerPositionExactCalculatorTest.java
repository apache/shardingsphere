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

package org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position.exact;

import org.apache.commons.text.RandomStringGenerator;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.UniqueKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.util.DataSourceTestUtils;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class InventoryIntegerPositionExactCalculatorTest {
    
    private static PipelineDataSource dataSource;
    
    @BeforeAll
    static void setUp() throws Exception {
        String databaseName = "pos_i_calc_" + RandomStringGenerator.builder().withinRange('a', 'z').build().generate(9);
        dataSource = new PipelineDataSource(DataSourceTestUtils.createHikariDataSource(databaseName), TypedSPILoader.getService(DatabaseType.class, "H2"));
        createTableAndInitData(dataSource);
    }
    
    @AfterAll
    static void tearDown() {
        dataSource.close();
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
            insertRecord(preparedStatement, 6, 11);
        }
    }
    
    private static void insertRecord(final PreparedStatement preparedStatement, final int userId, final int orderId) throws SQLException {
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, orderId);
        preparedStatement.setString(3, "OK");
        preparedStatement.executeUpdate();
    }
    
    @Test
    void assertGetPositionsWithOrderIdUniqueKey() {
        List<IngestPosition> actual = InventoryPositionExactCalculator.getPositions(new QualifiedTable(null, "t_order"), "order_id", 3, dataSource, new IntegerPositionHandler());
        assertThat(actual.size(), is(4));
        assertIntegerPosition0(actual.get(0), createIntegerPosition(1L, 3L));
        assertIntegerPosition0(actual.get(1), createIntegerPosition(4L, 6L));
        assertIntegerPosition0(actual.get(2), createIntegerPosition(7L, 9L));
        assertIntegerPosition0(actual.get(3), createIntegerPosition(10L, 11L));
    }
    
    private UniqueKeyIngestPosition<BigInteger> createIntegerPosition(final long lowerBound, final long upperBound) {
        return UniqueKeyIngestPosition.ofInteger(Range.closed(BigInteger.valueOf(lowerBound), BigInteger.valueOf(upperBound)));
    }
    
    private void assertIntegerPosition0(final IngestPosition actual, final UniqueKeyIngestPosition<BigInteger> expected) {
        assertThat(actual, isA(UniqueKeyIngestPosition.class));
        UniqueKeyIngestPosition<?> position = (UniqueKeyIngestPosition<?>) actual;
        assertThat(position.getType(), is(expected.getType()));
        assertThat(position.getLowerBound(), is(expected.getLowerBound()));
        assertThat(position.getUpperBound(), is(expected.getUpperBound()));
    }
    
    @Test
    void assertGetPositionsWithMultiColumnUniqueKeys() {
        List<IngestPosition> actual = InventoryPositionExactCalculator.getPositions(new QualifiedTable(null, "t_order"), "user_id", 3, dataSource, new IntegerPositionHandler());
        assertThat(actual.size(), is(2));
        assertIntegerPosition0(actual.get(0), createIntegerPosition(1L, 3L));
        assertIntegerPosition0(actual.get(1), createIntegerPosition(4L, 6L));
    }
}
