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

package org.apache.shardingsphere.test.e2e.data.pipeline.cases.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.data.pipeline.cases.base.BaseIncrementTask;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Slf4j
public final class CDCIncrementTask extends BaseIncrementTask {
    
    private final DataSource dataSource;
    
    private final String orderTableName;
    
    private final long minOrderId;
    
    private final long maxOrderId;
    
    private final int loopCount;
    
    @Override
    public void run() {
        try (Connection connection = dataSource.getConnection()) {
            for (int i = 0; i < loopCount; i++) {
                doOperation(connection);
            }
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void doOperation(final Connection connection) throws SQLException {
        PreparedStatement updateStatement = connection.prepareStatement(String.format("UPDATE %s SET status=?, t_date=? WHERE order_id = ?", orderTableName));
        updateStatement.setString(1, "CDC_OK");
        updateStatement.setDate(2, Date.valueOf(LocalDate.now().plus(1, ChronoUnit.DAYS)));
        updateStatement.setLong(3, ThreadLocalRandom.current().nextLong(minOrderId, maxOrderId));
        updateStatement.execute();
        PreparedStatement deleteStatement = connection.prepareStatement(String.format("DELETE FROM %s WHERE order_id = ?", orderTableName));
        deleteStatement.setLong(1, ThreadLocalRandom.current().nextLong(minOrderId, maxOrderId));
        deleteStatement.execute();
        PreparedStatement insertStatement = connection.prepareStatement(String.format("INSERT INTO %s(order_id, user_id, status) VALUES(?,?,?)", orderTableName));
        insertStatement.setLong(1, System.currentTimeMillis());
        insertStatement.setLong(2, 2);
        insertStatement.setString(3, "insertNow");
    }
}
