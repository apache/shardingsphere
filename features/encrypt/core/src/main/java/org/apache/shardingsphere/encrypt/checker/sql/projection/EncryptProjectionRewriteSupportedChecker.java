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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.List;

/**
 * Projection token generator rewrite supported checker.
 */
@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptProjectionRewriteSupportedChecker {
    
    /**
     * Check not contain encrypt projection in combine segment.
     *
     * @param encryptRule encrypt rule
     * @param selectStatementContext select statement context
     */
    public static void checkNotContainEncryptProjectionInCombineSegment(final EncryptRule encryptRule, final SelectStatementContext selectStatementContext) {
        ShardingSpherePreconditions.checkState(!containsEncryptProjectionInCombineSegment(encryptRule, selectStatementContext),
                () -> new UnsupportedSQLOperationException("Can not support encrypt projection in combine statement."));
    }
    
    private static boolean containsEncryptProjectionInCombineSegment(final EncryptRule encryptRule, final SelectStatementContext selectStatementContext) {
        if (!selectStatementContext.getSqlStatement().getCombine().isPresent()) {
            return false;
        }
        CombineSegment combineSegment = selectStatementContext.getSqlStatement().getCombine().get();
        List<Projection> leftProjections = selectStatementContext.getSubqueryContexts().get(combineSegment.getLeft().getStartIndex()).getProjectionsContext().getExpandProjections();
        List<Projection> rightProjections = selectStatementContext.getSubqueryContexts().get(combineSegment.getRight().getStartIndex()).getProjectionsContext().getExpandProjections();
        ShardingSpherePreconditions.checkState(leftProjections.size() == rightProjections.size(), () -> new UnsupportedSQLOperationException("Column projections must be same for combine statement"));
        for (int i = 0; i < leftProjections.size(); i++) {
            if (containsEncryptProjectionInCombineSegment(encryptRule, leftProjections.get(i), rightProjections.get(i))) {
                return true;
            }
        }
        return false;
    }
    
    private static boolean containsEncryptProjectionInCombineSegment(final EncryptRule encryptRule, final Projection leftProjection, final Projection rightProjection) {
        ColumnSegmentBoundInfo leftColumnInfo = getColumnSegmentBoundInfo(leftProjection);
        EncryptAlgorithm leftColumnEncryptor = encryptRule.findQueryEncryptor(leftColumnInfo.getOriginalTable().getValue(), leftColumnInfo.getOriginalColumn().getValue()).orElse(null);
        ColumnSegmentBoundInfo rightColumnInfo = getColumnSegmentBoundInfo(rightProjection);
        EncryptAlgorithm rightColumnEncryptor = encryptRule.findQueryEncryptor(rightColumnInfo.getOriginalTable().getValue(), rightColumnInfo.getOriginalColumn().getValue()).orElse(null);
        return null != leftColumnEncryptor || null != rightColumnEncryptor;
    }
    
    private static ColumnSegmentBoundInfo getColumnSegmentBoundInfo(final Projection projection) {
        return projection instanceof ColumnProjection
                ? new ColumnSegmentBoundInfo(null, null, ((ColumnProjection) projection).getOriginalTable(), ((ColumnProjection) projection).getOriginalColumn())
                : new ColumnSegmentBoundInfo(new IdentifierValue(projection.getColumnLabel()));
    }
    
    /**
     * Check not contain encrypt shorthand expand with subquery statement.
     *
     * @param selectStatementContext select statement context
     * @param projectionSegment projection segment
     */
    public static void checkNotContainEncryptShorthandExpandWithSubqueryStatement(final SelectStatementContext selectStatementContext, final ProjectionSegment projectionSegment) {
        ShardingSpherePreconditions.checkState(!(projectionSegment instanceof ShorthandProjectionSegment) || !selectStatementContext.containsTableSubquery(),
                () -> new UnsupportedSQLOperationException("Can not support encrypt shorthand expand with subquery statement."));
    }
}
