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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.metadata.CachedDatabaseMetaData;
import org.apache.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Encrypt data source.
 *
 * @author panjuan
 */
@Getter
@Slf4j
public class EncryptDataSource extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    private final DataSource dataSource;

    private final DatabaseMetaData cachedDatabaseMetaData;

    private final EncryptRule encryptRule;

    public EncryptDataSource(final DataSource dataSource, final EncryptRuleConfiguration encryptRuleConfiguration) throws SQLException {
        this.dataSource = dataSource;
        cachedDatabaseMetaData = createCachedDatabaseMetaData(dataSource);
        encryptRule = new EncryptRule(encryptRuleConfiguration);
    }

    private DatabaseMetaData createCachedDatabaseMetaData(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return new CachedDatabaseMetaData(connection.getMetaData());
        }
    }

    @Override
    @SneakyThrows
    public final Connection getConnection() {
        return dataSource.getConnection();
    }
}
