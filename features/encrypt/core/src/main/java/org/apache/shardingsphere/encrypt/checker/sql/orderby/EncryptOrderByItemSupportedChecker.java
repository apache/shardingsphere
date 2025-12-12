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

package org.apache.shardingsphere.encrypt.checker.sql.orderby;

import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Order by item supported checker for encrypt.
 */
@HighFrequencyInvocation
public final class EncryptOrderByItemSupportedChecker implements SupportedSQLChecker<SelectStatementContext, EncryptRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && containsOrderByItem((SelectStatementContext) sqlStatementContext);
    }
    
    private boolean containsOrderByItem(final SelectStatementContext sqlStatementContext) {
        if (!sqlStatementContext.getOrderByContext().getItems().isEmpty() && !sqlStatementContext.getOrderByContext().isGenerated()) {
            return true;
        }
        for (SelectStatementContext each : sqlStatementContext.getSubqueryContexts().values()) {
            if (containsOrderByItem(each)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void check(final EncryptRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final SelectStatementContext sqlStatementContext) {
        for (OrderByItem each : getOrderByItems(sqlStatementContext)) {
            if (each.getSegment() instanceof ColumnOrderByItemSegment) {
                checkColumnOrderByItem(rule, ((ColumnOrderByItemSegment) each.getSegment()).getColumn());
            }
        }
    }
    
    private Collection<OrderByItem> getOrderByItems(final SelectStatementContext sqlStatementContext) {
        Collection<OrderByItem> result = new LinkedList<>();
        if (!sqlStatementContext.getOrderByContext().isGenerated()) {
            result.addAll(sqlStatementContext.getOrderByContext().getItems());
        }
        for (SelectStatementContext each : sqlStatementContext.getSubqueryContexts().values()) {
            result.addAll(getOrderByItems(each));
        }
        return result;
    }
    
    private void checkColumnOrderByItem(final EncryptRule rule, final ColumnSegment columnSegment) {
        Optional<EncryptTable> encryptTable = rule.findEncryptTable(columnSegment.getColumnBoundInfo().getOriginalTable().getValue());
        String columnName = columnSegment.getIdentifier().getValue();
        ShardingSpherePreconditions.checkState(!encryptTable.isPresent() || !encryptTable.get().isEncryptColumn(columnName), () -> new UnsupportedEncryptSQLException("ORDER BY"));
    }
}
