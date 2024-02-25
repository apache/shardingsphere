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

package org.apache.shardingsphere.encrypt.rewrite.token.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.rule.column.EncryptColumn;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.bounded.ColumnSegmentBoundedInfo;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Iterator;

/**
 * Encrypt token generator utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptTokenGeneratorUtils {
    
    /**
     * Judge whether all join conditions use same encryptor or not.
     * 
     * @param joinConditions join conditions
     * @param encryptRule encrypt rule
     * @return whether all join conditions use same encryptor or not
     */
    public static boolean isAllJoinConditionsUseSameEncryptor(final Collection<BinaryOperationExpression> joinConditions, final EncryptRule encryptRule) {
        for (BinaryOperationExpression each : joinConditions) {
            if (!(each.getLeft() instanceof ColumnSegment) || !(each.getRight() instanceof ColumnSegment)) {
                continue;
            }
            EncryptAlgorithm leftColumnEncryptor = getColumnEncryptor(((ColumnSegment) each.getLeft()).getColumnBoundedInfo(), encryptRule);
            EncryptAlgorithm rightColumnEncryptor = getColumnEncryptor(((ColumnSegment) each.getRight()).getColumnBoundedInfo(), encryptRule);
            if (!isSameEncryptor(leftColumnEncryptor, rightColumnEncryptor)) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean isSameEncryptor(final EncryptAlgorithm leftColumnEncryptor, final EncryptAlgorithm rightColumnEncryptor) {
        if (null != leftColumnEncryptor && null != rightColumnEncryptor) {
            if (!leftColumnEncryptor.getType().equals(rightColumnEncryptor.getType())) {
                return false;
            }
            return leftColumnEncryptor.equals(rightColumnEncryptor);
        }
        return null == leftColumnEncryptor && null == rightColumnEncryptor;
    }
    
    private static EncryptAlgorithm getColumnEncryptor(final ColumnSegmentBoundedInfo columnBoundedInfo, final EncryptRule encryptRule) {
        String tableName = columnBoundedInfo.getOriginalTable().getValue();
        String columnName = columnBoundedInfo.getOriginalColumn().getValue();
        if (!encryptRule.findEncryptTable(tableName).isPresent() || !encryptRule.getEncryptTable(tableName).isEncryptColumn(columnName)) {
            return null;
        }
        EncryptTable encryptTable = encryptRule.getEncryptTable(tableName);
        EncryptColumn encryptColumn = encryptTable.getEncryptColumn(columnName);
        if (encryptColumn.getAssistedQuery().isPresent()) {
            return encryptColumn.getAssistedQuery().get().getEncryptor();
        }
        return encryptColumn.getCipher().getEncryptor();
    }
    
    /**
     * Judge whether all insert select columns use same encryptor or not.
     *
     * @param insertColumns insert columns
     * @param projections projections
     * @param encryptRule encrypt rule
     * @return whether all insert select columns use same encryptor or not 
     */
    public static boolean isAllInsertSelectColumnsUseSameEncryptor(final Collection<ColumnSegment> insertColumns, final Collection<Projection> projections, final EncryptRule encryptRule) {
        Iterator<ColumnSegment> insertColumnsIterator = insertColumns.iterator();
        Iterator<Projection> projectionIterator = projections.iterator();
        while (insertColumnsIterator.hasNext()) {
            ColumnSegment columnSegment = insertColumnsIterator.next();
            EncryptAlgorithm leftColumnEncryptor = getColumnEncryptor(columnSegment.getColumnBoundedInfo(), encryptRule);
            Projection projection = projectionIterator.next();
            ColumnSegmentBoundedInfo columnBoundedInfo = projection instanceof ColumnProjection
                    ? new ColumnSegmentBoundedInfo(null, null, ((ColumnProjection) projection).getOriginalTable(), ((ColumnProjection) projection).getOriginalColumn())
                    : new ColumnSegmentBoundedInfo(new IdentifierValue(projection.getColumnLabel()));
            EncryptAlgorithm rightColumnEncryptor = getColumnEncryptor(columnBoundedInfo, encryptRule);
            if (!isSameEncryptor(leftColumnEncryptor, rightColumnEncryptor)) {
                return false;
            }
        }
        return true;
    }
}
