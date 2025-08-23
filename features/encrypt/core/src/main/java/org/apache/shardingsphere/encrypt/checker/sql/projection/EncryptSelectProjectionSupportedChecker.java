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

package org.apache.shardingsphere.encrypt.checker.sql.projection;

import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.List;

/**
 * Select projection supported checker for encrypt.
 */
@HighFrequencyInvocation
public final class EncryptSelectProjectionSupportedChecker implements SupportedSQLChecker<SelectStatementContext, EncryptRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !sqlStatementContext.getTablesContext().getSimpleTables().isEmpty();
    }
    
    @Override
    public void check(final EncryptRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final SelectStatementContext sqlStatementContext) {
        checkSelectStatementContext(rule, sqlStatementContext);
        for (SelectStatementContext each : sqlStatementContext.getSubqueryContexts().values()) {
            checkSelectStatementContext(rule, each);
        }
    }
    
    private void checkSelectStatementContext(final EncryptRule rule, final SelectStatementContext selectStatementContext) {
        checkNotContainEncryptProjectionInCombineSegment(rule, selectStatementContext);
        for (ProjectionSegment each : selectStatementContext.getSqlStatement().getProjections().getProjections()) {
            checkNotContainEncryptShorthandExpandWithSubqueryStatement(selectStatementContext, each);
        }
    }
    
    private void checkNotContainEncryptProjectionInCombineSegment(final EncryptRule rule, final SelectStatementContext selectStatementContext) {
        ShardingSpherePreconditions.checkState(!containsEncryptProjectionInCombineSegment(rule, selectStatementContext),
                () -> new UnsupportedSQLOperationException("Can not support encrypt projection in combine statement."));
    }
    
    private boolean containsEncryptProjectionInCombineSegment(final EncryptRule rule, final SelectStatementContext selectStatementContext) {
        if (!selectStatementContext.getSqlStatement().getCombine().isPresent()) {
            return false;
        }
        CombineSegment combineSegment = selectStatementContext.getSqlStatement().getCombine().get();
        List<Projection> leftProjections = selectStatementContext.getSubqueryContexts().get(combineSegment.getLeft().getStartIndex()).getProjectionsContext().getExpandProjections();
        List<Projection> rightProjections = selectStatementContext.getSubqueryContexts().get(combineSegment.getRight().getStartIndex()).getProjectionsContext().getExpandProjections();
        ShardingSpherePreconditions.checkState(leftProjections.size() == rightProjections.size(), () -> new UnsupportedSQLOperationException("Column projections must be same for combine statement"));
        for (int i = 0; i < leftProjections.size(); i++) {
            if (containsEncryptProjectionInCombineSegment(rule, leftProjections.get(i), rightProjections.get(i))) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsEncryptProjectionInCombineSegment(final EncryptRule rule, final Projection leftProjection, final Projection rightProjection) {
        ColumnSegmentBoundInfo leftColumnInfo = getColumnSegmentBoundInfo(leftProjection);
        EncryptAlgorithm leftColumnEncryptor = rule.findQueryEncryptor(leftColumnInfo.getOriginalTable().getValue(), leftColumnInfo.getOriginalColumn().getValue()).orElse(null);
        ColumnSegmentBoundInfo rightColumnInfo = getColumnSegmentBoundInfo(rightProjection);
        EncryptAlgorithm rightColumnEncryptor = rule.findQueryEncryptor(rightColumnInfo.getOriginalTable().getValue(), rightColumnInfo.getOriginalColumn().getValue()).orElse(null);
        return null != leftColumnEncryptor || null != rightColumnEncryptor;
    }
    
    private ColumnSegmentBoundInfo getColumnSegmentBoundInfo(final Projection projection) {
        return projection instanceof ColumnProjection
                ? new ColumnSegmentBoundInfo(null, ((ColumnProjection) projection).getOriginalTable(), ((ColumnProjection) projection).getOriginalColumn(),
                        ((ColumnProjection) projection).getColumnBoundInfo().getTableSourceType())
                : new ColumnSegmentBoundInfo(new IdentifierValue(projection.getColumnLabel()));
    }
    
    private void checkNotContainEncryptShorthandExpandWithSubqueryStatement(final SelectStatementContext selectStatementContext, final ProjectionSegment projectionSegment) {
        ShardingSpherePreconditions.checkState(!(projectionSegment instanceof ShorthandProjectionSegment) || !selectStatementContext.containsTableSubquery(),
                () -> new UnsupportedSQLOperationException("Can not support encrypt shorthand expand with subquery statement."));
    }
}
