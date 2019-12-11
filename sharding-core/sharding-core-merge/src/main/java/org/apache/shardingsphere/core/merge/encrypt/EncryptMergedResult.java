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

package org.apache.shardingsphere.core.merge.encrypt;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.merge.MergedResult;
import org.apache.shardingsphere.core.merge.MergedResultMetaData;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.spi.encrypt.ShardingEncryptor;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.DescribeStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowColumnsStatement;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * Merged result for encrypt.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class EncryptMergedResult implements MergedResult {
    
    private final MergedResultMetaData metaData;
    
    private final MergedResult mergedResult;

    private final EncryptRule encryptRule;

    private final SQLStatementContext sqlStatementContext;
    
    private final boolean queryWithCipherColumn;
    
    @Override
    public boolean next() throws SQLException {
        return mergedResult.next();
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) throws SQLException {
        Object value = mergedResult.getValue(columnIndex, type);
        if (null == value || !queryWithCipherColumn) {
            return value;
        }
        if (sqlStatementContext.getSqlStatement() instanceof ShowColumnsStatement
                || sqlStatementContext.getSqlStatement() instanceof DescribeStatement) {
            String logicTable = sqlStatementContext.getTablesContext().getSingleTableName();
            if (encryptRule.getCipherColumns(logicTable).contains(String.valueOf(value))) {
                return encryptRule.getLogicColumnOfCipher(logicTable, String.valueOf(value));
            }
            if (encryptRule.getPlainColumns(logicTable).contains(String.valueOf(value))) {
                return encryptRule.getLogicColumnOfPlain(logicTable, String.valueOf(value));
            }
            return value;
        }
        Optional<ShardingEncryptor> encryptor = metaData.findEncryptor(columnIndex);
        return encryptor.isPresent() ? encryptor.get().decrypt(value.toString()) : value;
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) throws SQLException {
        return mergedResult.getCalendarValue(columnIndex, type, calendar);
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) throws SQLException {
        return mergedResult.getInputStream(columnIndex, type);
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return mergedResult.wasNull();
    }
}
