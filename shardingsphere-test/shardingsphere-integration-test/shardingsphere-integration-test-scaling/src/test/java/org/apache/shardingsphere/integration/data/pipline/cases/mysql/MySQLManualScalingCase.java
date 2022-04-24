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

package org.apache.shardingsphere.integration.data.pipline.cases.mysql;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.integration.data.pipline.cases.BaseScalingITCase;
import org.apache.shardingsphere.integration.data.pipline.cases.command.mysql.MySQLCommand;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXB;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * MySQL auto rule scaling test case.
 */
@Slf4j
public final class MySQLManualScalingCase extends BaseScalingITCase {
    
    private static final SnowflakeKeyGenerateAlgorithm SNOWFLAKE_GENERATE = new SnowflakeKeyGenerateAlgorithm();
    
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();
    
    private static MySQLCommand mySQLCommand;
    
    public MySQLManualScalingCase() {
        super(new MySQLDatabaseType());
    }
    
    @BeforeClass
    public static void beforeClass() {
        mySQLCommand = JAXB.unmarshal(MySQLManualScalingCase.class.getClassLoader().getResource("env/mysql/sql.xml"), MySQLCommand.class);
    }
    
    @Before
    public void init() throws SQLException {
        super.init();
        try (Connection connection = getProxyConnection("sharding_db")) {
            connection.createStatement().execute(mySQLCommand.getCreateTableOrder());
            connection.createStatement().execute(mySQLCommand.getCreateTableOrderItem());
            // init date, need more than 3000 rows, in order to test certain conditions
            initTableData(connection, mySQLCommand.getInsertOrder(), mySQLCommand.getInsertOrderItem());
        }
    }
    
    @Override
    protected void initTableData(final Connection connection, final String insertOrderSQL, final String insertOrderItemSQL) throws SQLException {
        PreparedStatement orderStatement = connection.prepareStatement(insertOrderSQL);
        PreparedStatement itemStatement = connection.prepareStatement(insertOrderItemSQL);
        for (int i = 1; i <= 3000; i++) {
            orderStatement.setLong(1, (Long) SNOWFLAKE_GENERATE.generateKey());
            int orderId = RANDOM.nextInt(0, 5);
            orderStatement.setInt(2, orderId);
            int userId = RANDOM.nextInt(0, 5);
            orderStatement.setInt(3, userId);
            orderStatement.setString(4, "varchar" + i);
            orderStatement.setByte(5, (byte) 1);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            orderStatement.setTimestamp(6, timestamp);
            orderStatement.setTimestamp(7, timestamp);
            orderStatement.setBytes(8, "hello".getBytes(StandardCharsets.UTF_8));
            orderStatement.setBinaryStream(9, null);
            orderStatement.setBigDecimal(10, new BigDecimal("100.00"));
            orderStatement.setString(11, "test");
            orderStatement.setDouble(12, Math.random());
            orderStatement.setString(13, "{}");
            orderStatement.addBatch();
            itemStatement.setLong(1, (Long) SNOWFLAKE_GENERATE.generateKey());
            itemStatement.setInt(2, orderId);
            itemStatement.setInt(3, userId);
            itemStatement.setString(4, "SUCCESS");
            itemStatement.addBatch();
        }
        orderStatement.executeBatch();
        itemStatement.executeBatch();
    }
    
    @Test
    public void test() throws SQLException, InterruptedException {
        try (Connection connection = getProxyConnection("sharding_db")) {
            ResultSet previewResult = connection.createStatement().executeQuery(getCommonSQLCommand().getPreviewSelectOrder());
            List<String> actualSourceNodes = Lists.newLinkedList();
            while (previewResult.next()) {
                actualSourceNodes.add(previewResult.getString(1));
            }
            assertThat(actualSourceNodes, is(Lists.newArrayList("ds_0", "ds_1")));
            connection.createStatement().execute(getCommonSQLCommand().getAlterShardingAlgorithm());
            connection.createStatement().execute(getCommonSQLCommand().getAlterShardingTableRule());
            ResultSet scalingList = connection.createStatement().executeQuery(getCommonSQLCommand().getShowScalingList());
            assertTrue(scalingList.next());
            String jobId = scalingList.getString(1);
            checkMatchConsistency(connection, jobId);
        }
    }
}
