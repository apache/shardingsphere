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

package org.apache.shardingsphere.proxy.backend.hbase.converter;

import lombok.RequiredArgsConstructor;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import org.apache.shardingsphere.proxy.backend.hbase.context.HBaseContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HBase database insert converter.
 */
@RequiredArgsConstructor
public final class HBaseDatabaseInsertConverter implements HBaseDatabaseConverter {
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    /**
     * Convert SQL statement to HBase operation.
     *
     * @return HBase operation
     */
    @Override
    public HBaseOperation convert() {
        InsertStatementContext context = (InsertStatementContext) sqlStatementContext;
        String tableName = context.getTablesContext().getTableNames().iterator().next();
        return new HBaseOperation(tableName, new HBaseInsertOperationAdapter(createHBaseRequest(context)));
    }
    
    private Put generateHBaseRequest(final InsertStatementContext context, final InsertValueContext insertValueContext) {
        List<String> columns = context.getInsertColumnNames();
        List<Object> values = insertValueContext.getValueExpressions().stream().map(each -> ((LiteralExpressionSegment) each).getLiterals()).collect(Collectors.toList());
        Put result = new Put(Bytes.toBytes(String.valueOf(values.get(0))));
        for (int i = 1; i < columns.size(); i++) {
            result.addColumn(Bytes.toBytes(HBaseContext.getInstance().getColumnFamily()), Bytes.toBytes(String.valueOf(columns.get(i))), Bytes.toBytes(String.valueOf(values.get(i))));
        }
        return result;
    }
    
    private List<Put> createHBaseRequest(final InsertStatementContext context) {
        return context.getInsertValueContexts().stream().map(each -> generateHBaseRequest(context, each)).collect(Collectors.toList());
    }
}
