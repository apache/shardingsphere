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
import org.apache.shardingsphere.data.pipeline.cdc.client.sqlbuilder.SQLBuilder;
import org.apache.shardingsphere.data.pipeline.cdc.client.util.AnyValueConvert;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.DataRecordResult.Record;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class AbstractDataSourceImporter implements Importer {
    
    protected abstract Connection getConnection();
    
    protected abstract SQLBuilder getSQLBuilder();
    
    @Override
    public void write(final Record record) throws Exception {
        Optional<String> sqlOptional = buildSQL(record);
        if (!sqlOptional.isPresent()) {
            log.error("build sql failed, record {}", record);
            throw new RuntimeException("build sql failed");
        }
        String sql = sqlOptional.get();
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
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
    
    protected Optional<String> buildSQL(final Record record) {
        switch (record.getDataChangeType()) {
            case INSERT:
                return Optional.ofNullable(getSQLBuilder().buildInsertSQL(record));
            case UPDATE:
                return Optional.ofNullable(getSQLBuilder().buildUpdateSQL(record));
            case DELETE:
                return Optional.ofNullable(getSQLBuilder().buildDeleteSQL(record));
            default:
                return Optional.empty();
        }
    }
}
