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

package org.apache.shardingsphere.encrypt.checker.cryptographic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.rewrite.token.comparator.EncryptorComparator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ParameterMarkerProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.SubqueryProjection;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Iterator;

/**
 * Insert select columns encryptor comparator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InsertSelectColumnsEncryptorChecker {
    
    /**
     * Check whether same encryptor.
     *
     * @param insertColumns insert columns
     * @param projections projections
     * @param encryptRule encrypt rule
     */
    public static void checkIsSame(final Collection<ColumnSegment> insertColumns, final Collection<Projection> projections, final EncryptRule encryptRule) {
        Iterator<ColumnSegment> insertColumnsIterator = insertColumns.iterator();
        Iterator<Projection> projectionIterator = projections.iterator();
        while (insertColumnsIterator.hasNext()) {
            ColumnSegment insertColumnSegment = insertColumnsIterator.next();
            Projection projection = projectionIterator.next();
            if (isLiteralOrParameterMarker(projection)) {
                continue;
            }
            ColumnSegmentBoundInfo projectionColumnBoundInfo = getColumnSegmentBoundInfo(projection);
            EncryptAlgorithm insertColumnEncryptor = encryptRule.findQueryEncryptor(
                    insertColumnSegment.getColumnBoundInfo().getOriginalTable().getValue(), insertColumnSegment.getColumnBoundInfo().getOriginalColumn().getValue()).orElse(null);
            EncryptAlgorithm projectionEncryptor =
                    encryptRule.findQueryEncryptor(projectionColumnBoundInfo.getOriginalTable().getValue(), projectionColumnBoundInfo.getOriginalColumn().getValue()).orElse(null);
            ShardingSpherePreconditions.checkState(EncryptorComparator.isSame(insertColumnEncryptor, projectionEncryptor), () -> new UnsupportedSQLOperationException(
                    "Can not use different encryptor for " + insertColumnSegment.getColumnBoundInfo() + " and " + projectionColumnBoundInfo + " in insert select columns"));
        }
    }
    
    /**
     * Compare whether same encryptor.
     *
     * @param insertColumns insert columns
     * @param projections projections
     * @param encryptRule encrypt rule
     * @return same encryptors or not
     */
    public static boolean isSame(final Collection<ColumnSegment> insertColumns, final Collection<Projection> projections, final EncryptRule encryptRule) {
        Iterator<ColumnSegment> insertColumnsIterator = insertColumns.iterator();
        Iterator<Projection> projectionIterator = projections.iterator();
        while (insertColumnsIterator.hasNext()) {
            ColumnSegment insertColumnSegment = insertColumnsIterator.next();
            Projection projection = projectionIterator.next();
            if (isLiteralOrParameterMarker(projection)) {
                continue;
            }
            ColumnSegmentBoundInfo projectionColumnBoundInfo = getColumnSegmentBoundInfo(projection);
            EncryptAlgorithm insertColumnEncryptor = encryptRule.findQueryEncryptor(
                    insertColumnSegment.getColumnBoundInfo().getOriginalTable().getValue(), insertColumnSegment.getColumnBoundInfo().getOriginalColumn().getValue()).orElse(null);
            EncryptAlgorithm projectionEncryptor =
                    encryptRule.findQueryEncryptor(projectionColumnBoundInfo.getOriginalTable().getValue(), projectionColumnBoundInfo.getOriginalColumn().getValue()).orElse(null);
            if (!EncryptorComparator.isSame(insertColumnEncryptor, projectionEncryptor)) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean isLiteralOrParameterMarker(final Projection projection) {
        if (projection instanceof ExpressionProjection) {
            ExpressionSegment expressionSegment = ((ExpressionProjection) projection).getExpressionSegment().getExpr();
            return expressionSegment instanceof LiteralExpressionSegment;
        }
        return projection instanceof ParameterMarkerProjection;
    }
    
    private static ColumnSegmentBoundInfo getColumnSegmentBoundInfo(final Projection projection) {
        if (projection instanceof ColumnProjection) {
            return ((ColumnProjection) projection).getColumnBoundInfo();
        }
        if (projection instanceof SubqueryProjection) {
            return getColumnSegmentBoundInfo(((SubqueryProjection) projection).getProjection());
        }
        return new ColumnSegmentBoundInfo(new IdentifierValue(projection.getColumnLabel()));
    }
}
