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

import com.google.protobuf.Any;
import com.google.protobuf.ProtocolStringList;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.client.parameter.ImportDataSourceParameter;
import org.apache.shardingsphere.data.pipeline.cdc.client.sqlbuilder.SQLBuilder;
import org.apache.shardingsphere.data.pipeline.cdc.client.sqlbuilder.SQLBuilderFactory;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.AnyValueConvert;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data source importer.
 */
@Slf4j
public final class DataSourceImporter implements Importer {
    
    private final Connection connection;
    
    private final SQLBuilder sqlBuilder;
    
    public DataSourceImporter(final String databaseType, final ImportDataSourceParameter dataSourceParam) {
        String jdbcUrl = Optional.ofNullable(dataSourceParam.getJdbcUrl()).orElseThrow(() -> new IllegalArgumentException("jdbcUrl is null"));
        String username = Optional.ofNullable(dataSourceParam.getUsername()).orElseThrow(() -> new IllegalArgumentException("username is null"));
        String password = Optional.ofNullable(dataSourceParam.getPassword()).orElseThrow(() -> new IllegalArgumentException("password is null"));
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
        sqlBuilder = SQLBuilderFactory.getSQLBuilder(databaseType);
    }
    
    @Override
    public void write(final Record record) throws Exception {
        Optional<String> sqlOptional = buildSQL(record);
        if (!sqlOptional.isPresent()) {
            log.error("build sql failed, record {}", record);
            throw new RuntimeException("build sql failed");
        }
        String sql = sqlOptional.get();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            List<Any> afterValue = new ArrayList<>(record.getAfterMap().values());
            ProtocolStringList uniqueKeyNamesList = record.getTableMetaData().getUniqueKeyNamesList();
            List<String> conditionColumnNames = record.getBeforeMap().keySet().containsAll(uniqueKeyNamesList) ? uniqueKeyNamesList : new ArrayList<>(record.getBeforeMap().keySet());
            switch (record.getDataChangeType()) {
                case INSERT:
                    for (int i = 0; i < afterValue.size(); i++) {
                        preparedStatement.setObject(i + 1, AnyValueConvert.convertToObject(afterValue.get(i)));
                    }
                    break;
                case UPDATE:
                    for (int i = 0; i < afterValue.size(); i++) {
                        preparedStatement.setObject(i + 1, AnyValueConvert.convertToObject(afterValue.get(i)));
                    }
                    for (int i = 0; i < conditionColumnNames.size(); i++) {
                        preparedStatement.setObject(afterValue.size() + i + 1, AnyValueConvert.convertToObject(record.getBeforeMap().get(conditionColumnNames.get(i))));
                    }
                    int updateCount = preparedStatement.executeUpdate();
                    if (1 != updateCount) {
                        log.warn("executeUpdate failed, updateCount={}, updateSql={}, updatedColumns={}, conditionColumns={}", updateCount, sql, record.getAfterMap().keySet(), conditionColumnNames);
                    }
                    break;
                case DELETE:
                    for (int i = 0; i < conditionColumnNames.size(); i++) {
                        preparedStatement.setObject(i + 1, AnyValueConvert.convertToObject(record.getAfterMap().get(conditionColumnNames.get(i))));
                    }
                    preparedStatement.execute();
                    break;
                default:
            }
            preparedStatement.execute();
        }
    }
    
    private Optional<String> buildSQL(final Record record) {
        switch (record.getDataChangeType()) {
            case INSERT:
                return Optional.ofNullable(sqlBuilder.buildInsertSQL(record));
            case UPDATE:
                return Optional.ofNullable(sqlBuilder.buildUpdateSQL(record));
            case DELETE:
                return Optional.ofNullable(sqlBuilder.buildDeleteSQL(record));
            default:
                return Optional.empty();
        }
    }
    
    @Override
    public void close() throws Exception {
        connection.close();
    }
}
