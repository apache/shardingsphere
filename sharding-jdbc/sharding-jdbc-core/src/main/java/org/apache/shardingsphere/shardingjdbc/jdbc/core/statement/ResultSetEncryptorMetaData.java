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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.statement;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.merge.dql.EncryptorMetaData;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Encryptor meta data for query header.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ResultSetEncryptorMetaData implements EncryptorMetaData {
    
    private final EncryptRule encryptRule;
    
    private final ResultSetMetaData resultSetMetaData;
    
    private final SQLStatementContext sqlStatementContext;
    
    @Override
    public Optional<ShardingEncryptor> findEncryptor(final int columnIndex) throws SQLException {
        String columnName = resultSetMetaData.getColumnName(columnIndex);
        for (String each : sqlStatementContext.getTablesContext().getTableNames()) {
            Optional<ShardingEncryptor> result = encryptRule.isCipherColumn(each, columnName)
                    ? encryptRule.findShardingEncryptor(each, encryptRule.getLogicColumnOfCipher(each, columnName)) : Optional.<ShardingEncryptor>absent();
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }
}
