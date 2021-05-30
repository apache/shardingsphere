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

package org.apache.shardingsphere.integration.scaling.test.mysql.fixture;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.integration.scaling.test.mysql.util.SourceShardingSphereUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@RequiredArgsConstructor
public final class FixtureWriteThread implements Runnable {
    
    private static final String INSERT_SQL = "INSERT INTO t1(c1, c2) VALUES(?, ?)";
    
    private static final String UPDATE_SQL = "UPDATE t1 SET c2 = ? WHERE c1 = ?";
    
    private static final String DELETE_SQL = "DELETE FROM t1 WHERE c1 = ?";
    
    private final long writeThreadTimeout;
    
    private final long writeSpawn;
    
    private boolean running;
    
    private Thread thread;
    
    /**
     * Start.
     */
    public void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }
    
    /**
     * Stop.
     *
     * @throws InterruptedException interrupted exception
     */
    public void stop() throws InterruptedException {
        running = false;
        thread.interrupt();
        thread.join();
    }
    
    @SneakyThrows
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        int idGenerator = 0;
        DataSource dataSource = SourceShardingSphereUtil.createHostDataSource();
        while (running && !checkTimeout(startTime, writeThreadTimeout)) {
            try (Connection connection = dataSource.getConnection()) {
                insert(connection, ++idGenerator);
                update(connection, idGenerator);
                insert(connection, ++idGenerator);
                delete(connection, idGenerator);
            }
            try {
                Thread.sleep(writeSpawn);
            } catch (InterruptedException ignored) {
            }
        }
    }
    
    private boolean checkTimeout(final long startTime, final long timeout) {
        return timeout < System.currentTimeMillis() - startTime;
    }
    
    private void insert(final Connection connection, final int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
        ps.setInt(1, id);
        ps.setString(2, Integer.toString(id));
        ps.execute();
    }
    
    private void update(final Connection connection, final int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(UPDATE_SQL);
        ps.setString(1, Integer.toString(id + 1));
        ps.setInt(2, id);
        ps.execute();
    }
    
    private void delete(final Connection connection, final int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(DELETE_SQL);
        ps.setInt(1, id);
        ps.execute();
    }
}
