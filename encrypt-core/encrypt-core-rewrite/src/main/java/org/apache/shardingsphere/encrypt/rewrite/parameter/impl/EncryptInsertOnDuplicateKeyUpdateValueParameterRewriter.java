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

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.rewrite.aware.QueryWithCipherColumnAware;
import org.apache.shardingsphere.encrypt.rewrite.parameter.EncryptParameterRewriter;
import org.apache.shardingsphere.encrypt.strategy.spi.Encryptor;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.impl.GroupedParameterBuilder;

import java.util.Collection;
import java.util.List;

/**
 * Insert on duplicate key update parameter rewriter for encrypt.
 *
 * @author chun.yang
 */
@Setter
public final class EncryptInsertOnDuplicateKeyUpdateValueParameterRewriter extends EncryptParameterRewriter implements QueryWithCipherColumnAware {

    private boolean queryWithCipherColumn;

    @Override
    protected boolean isNeedRewriteForEncrypt(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertSQLStatementContext && sqlStatementContext.getSqlStatement().findSQLSegment(OnDuplicateKeyColumnsSegment.class).isPresent();
    }

    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final SQLStatementContext sqlStatementContext, final List<Object> parameters) {
        String tableName = sqlStatementContext.getTablesContext().getSingleTableName();
        OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = sqlStatementContext.getSqlStatement().findSQLSegment(OnDuplicateKeyColumnsSegment.class).get();
        Collection<AssignmentSegment> onDuplicateKeyColumnsSegments = onDuplicateKeyColumnsSegment.getColumns();
        if (onDuplicateKeyColumnsSegments.isEmpty()) {
            return;
        }
        GroupedParameterBuilder groupedParameterBuilder = (GroupedParameterBuilder) parameterBuilder;
        for (AssignmentSegment each : onDuplicateKeyColumnsSegments) {
            ExpressionSegment expressionSegment = each.getValue();
            Object cipherColumnValue = null;
            Object plainColumnValue = null;
            if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
                plainColumnValue = parameters.get(((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
            }
            if (queryWithCipherColumn) {
                Optional<Encryptor> encryptor = getEncryptRule().findEncryptor(tableName, each.getColumn().getName());
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
