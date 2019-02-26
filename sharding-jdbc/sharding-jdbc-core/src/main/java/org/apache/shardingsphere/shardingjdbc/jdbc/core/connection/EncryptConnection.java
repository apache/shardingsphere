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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.connection;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.metadata.CachedDatabaseMetaData;
import org.apache.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationConnection;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * Encrypt connection.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
public final class EncryptConnection extends AbstractUnsupportedOperationConnection {

    private final Connection connection;
    
    private final EncryptRule encryptRule;
    
    private final ShardingTableMetaData encryptTableMetaData;
    
    @Override
    @SneakyThrows
    public DatabaseMetaData getMetaData() {
        return connection.getMetaData();
    }
    
    @Override
    @SneakyThrows
    public boolean getAutoCommit() {
        return connection.getAutoCommit();
    }
    
    @Override
    @SneakyThrows
    public void setAutoCommit(final boolean autoCommit) {
        connection.setAutoCommit(autoCommit);
    }
    
    @Override
    @SneakyThrows
    public void commit() {
        connection.commit();
    }
    
    @Override
    @SneakyThrows
    public void rollback() {
        connection.rollback();
    }
    
    @Override
    @SneakyThrows
    public void close() {
        connection.close();
    }
    
    @Override
    @SneakyThrows
    public boolean isReadOnly() {
        return connection.isReadOnly();
    }
    
    @Override
    @SneakyThrows
    public void setReadOnly(final boolean readOnly) {
        connection.setReadOnly(readOnly);
    }
    
    @Override
    @SneakyThrows
    public int getTransactionIsolation() {
        return connection.getTransactionIsolation();
    }
    
    @Override
    @SneakyThrows
    public void setTransactionIsolation(final int level) {
        connection.setTransactionIsolation(level);
    }
    
    @Override
    public SQLWarning getWarnings() {
        return null;
    }
    
    @Override
    public void clearWarnings() {
    }
    
    @Override
    public int getHoldability() {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }
    
    @Override
    public void setHoldability(final int holdability) {
    }
}
