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

package org.apache.shardingsphere.encrypt.rewrite.parameter.impl;

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.encrypt.rewrite.parameter.EncryptParameterRewriter;
import org.apache.shardingsphere.encrypt.strategy.spi.Encryptor;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.impl.GroupedParameterBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Insert on duplicate key update parameter rewriter for encrypt.
 */
@Setter
public final class EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter extends EncryptParameterRewriter<InsertStatementContext> implements QueryWithCipherColumnAware {
    
    private boolean queryWithCipherColumn;
    
    @Override
    protected boolean isNeedRewriteForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && ((InsertStatementContext) sqlStatementContext).getSqlStatement().getOnDuplicateKeyColumns().isPresent();
    }
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final InsertStatementContext insertStatementContext, final List<Object> parameters) {
        String tableName = insertStatementContext.getSqlStatement().getTable().getTableName().getIdentifier().getValue();
        Preconditions.checkState(insertStatementContext.getSqlStatement().getOnDuplicateKeyColumns().isPresent());
        OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = insertStatementContext.getSqlStatement().getOnDuplicateKeyColumns().get();
        Collection<AssignmentSegment> onDuplicateKeyColumnsSegments = onDuplicateKeyColumnsSegment.getColumns();
        if (onDuplicateKeyColumnsSegments.isEmpty()) {
            return;
        }
        GroupedParameterBuilder groupedParameterBuilder = (GroupedParameterBuilder) parameterBuilder;
        for (AssignmentSegment each : onDuplicateKeyColumnsSegments) {
            ExpressionSegment expressionSegment = each.getValue();
            Object cipherColumnValue;
            Object plainColumnValue = null;
            if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
                plainColumnValue = parameters.get(((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
            }
            if (queryWithCipherColumn) {
                Optional<Encryptor> encryptor = getEncryptRule().findEncryptor(tableName, each.getColumn().getIdentifier().getValue());
                if (encryptor.isPresent()) {
                    cipherColumnValue = encryptor.get().encrypt(plainColumnValue);
                    groupedParameterBuilder.getOnDuplicateKeyUpdateAddedParameters().add(cipherColumnValue);
                }
            }
            if (null != plainColumnValue) {
                groupedParameterBuilder.getOnDuplicateKeyUpdateAddedParameters().add(plainColumnValue);
            }
        }
    }
}
