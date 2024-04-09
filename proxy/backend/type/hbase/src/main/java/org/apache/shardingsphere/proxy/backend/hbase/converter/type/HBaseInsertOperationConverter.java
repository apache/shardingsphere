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

package org.apache.shardingsphere.proxy.backend.hbase.converter.type;

import lombok.RequiredArgsConstructor;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import org.apache.shardingsphere.proxy.backend.hbase.context.HBaseContext;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.operation.HBaseInsertOperation;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HBase insert operation converter.
 */
@RequiredArgsConstructor
public final class HBaseInsertOperationConverter implements HBaseOperationConverter {
    
    private final SQLStatementContext sqlStatementContext;
    
    @Override
    public HBaseOperation convert() {
        InsertStatementContext insertStatementContext = (InsertStatementContext) sqlStatementContext;
        String tableName = insertStatementContext.getTablesContext().getTableNames().iterator().next();
        return new HBaseOperation(tableName, new HBaseInsertOperation(createHBaseRequest(insertStatementContext)));
    }
    
    private List<Put> createHBaseRequest(final InsertStatementContext insertStatementContext) {
        return insertStatementContext.getInsertValueContexts().stream().map(each -> generateHBaseRequest(insertStatementContext, each)).collect(Collectors.toList());
    }
    
    private Put generateHBaseRequest(final InsertStatementContext insertStatementContext, final InsertValueContext insertValueContext) {
        List<String> columns = insertStatementContext.getInsertColumnNames();
        List<Object> values = insertValueContext.getValueExpressions().stream().map(each -> ((LiteralExpressionSegment) each).getLiterals()).collect(Collectors.toList());
        Put result = new Put(Bytes.toBytes(String.valueOf(values.get(0))));
        for (int i = 1; i < columns.size(); i++) {
            result.addColumn(Bytes.toBytes(HBaseContext.getInstance().getColumnFamily()), Bytes.toBytes(String.valueOf(columns.get(i))), Bytes.toBytes(String.valueOf(values.get(i))));
        }
        return result;
    }
}
