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

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseOperation;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseOperationConverter;
import org.apache.shardingsphere.proxy.backend.hbase.converter.HBaseRowKeyExtractor;
import org.apache.shardingsphere.proxy.backend.hbase.converter.operation.HBaseDeleteOperation;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * HBase delete operation converter.
 */
@RequiredArgsConstructor
public final class HBaseDeleteOperationConverter implements HBaseOperationConverter {
    
    private final SQLStatementContext sqlStatementContext;
    
    @Override
    public HBaseOperation convert() {
        DeleteStatementContext deleteStatementContext = (DeleteStatementContext) sqlStatementContext;
        Optional<WhereSegment> whereSegment = deleteStatementContext.getWhereSegments().stream().findFirst();
        Preconditions.checkArgument(whereSegment.isPresent(), "Where segment is absent.");
        return whereSegment.get().getExpr() instanceof InExpression
                ? createDeleteMultipleRowKeysOperation(deleteStatementContext, whereSegment.get())
                : createDeleteSingleRowKeyOperation(deleteStatementContext, whereSegment.get());
    }
    
    private HBaseOperation createDeleteMultipleRowKeysOperation(final DeleteStatementContext deleteStatementContext, final WhereSegment whereSegment) {
        String tableName = deleteStatementContext.getTablesContext().getTableNames().iterator().next();
        List<String> rowKeys = HBaseRowKeyExtractor.getRowKeys((InExpression) whereSegment.getExpr());
        List<Delete> deletes = rowKeys.stream().map(each -> new Delete(Bytes.toBytes(each))).collect(Collectors.toList());
        return new HBaseOperation(tableName, new HBaseDeleteOperation(deletes));
    }
    
    private HBaseOperation createDeleteSingleRowKeyOperation(final DeleteStatementContext deleteStatementContext, final WhereSegment whereSegment) {
        String tableName = deleteStatementContext.getTablesContext().getTableNames().iterator().next();
        String rowKey = HBaseRowKeyExtractor.getRowKey((BinaryOperationExpression) whereSegment.getExpr());
        Delete delete = new Delete(Bytes.toBytes(rowKey));
        return new HBaseOperation(tableName, delete);
    }
}
