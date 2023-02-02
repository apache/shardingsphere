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

package org.apache.shardingsphere.data.pipeline.cdc.client.importer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.ImportDataSourceParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.sqlbuilder.SQLBuilder;
import org.apache.shardingsphere.data.pipeline.cdc.client.sqlbuilder.SQLBuilderFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

/**
 * MySQL importer.
 */
@RequiredArgsConstructor
@Slf4j
public final class MySQLImporter extends AbstractDataSourceImporter {
    
    private final SQLBuilder sqlBuilder = SQLBuilderFactory.getSQLBuilder("MySQL");
    
    @Getter
    private final Connection connection;
    
    public MySQLImporter(final ImportDataSourceParameter dataSourceParameter) {
        String url = Optional.ofNullable(dataSourceParameter.getUrl()).orElse("localhost");
        String port = Optional.ofNullable(dataSourceParameter.getPort()).orElse(3306).toString();
        String database = Optional.ofNullable(dataSourceParameter.getDatabase()).orElse("cdc_db");
        String username = Optional.ofNullable(dataSourceParameter.getUsername()).orElse("test_user");
        String password = Optional.ofNullable(dataSourceParameter.getPassword()).orElse("Root@123");
        try {
            connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true", url, port, database), username, password);
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    protected SQLBuilder getSQLBuilder() {
        return sqlBuilder;
    }
    
    @Override
    public void close() throws Exception {
        connection.close();
    }
}
