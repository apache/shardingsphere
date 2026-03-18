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

package org.apache.shardingsphere.infra.binder.engine.segment.util;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.extractor.ProjectionIdentifierExtractEngine;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Subquery table bind utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubqueryTableBindUtils {
    
    /**
     * Create subquery projections.
     *
     * @param projections projections
     * @param subqueryTableName subquery table name
     * @param databaseType database type
     * @param tableSourceType table source type
     * @return subquery projections
     */
    public static Collection<ProjectionSegment> createSubqueryProjections(final Collection<ProjectionSegment> projections, final IdentifierValue subqueryTableName,
                                                                          final DatabaseType databaseType, final TableSourceType tableSourceType) {
        Collection<ProjectionSegment> result = new LinkedList<>();
        for (ProjectionSegment each : projections) {
            if (each instanceof ColumnProjectionSegment) {
                result.add(createColumnProjection((ColumnProjectionSegment) each, subqueryTableName, tableSourceType));
            } else if (each instanceof ShorthandProjectionSegment) {
                result.addAll(createSubqueryProjections(((ShorthandProjectionSegment) each).getActualProjectionSegments(), subqueryTableName, databaseType, tableSourceType));
            } else if (each instanceof ExpressionProjectionSegment) {
                result.add(createColumnProjection((ExpressionProjectionSegment) each, subqueryTableName, databaseType));
            } else if (each instanceof AggregationProjectionSegment) {
                result.add(createColumnProjection((AggregationProjectionSegment) each, subqueryTableName, databaseType));
            } else {
                result.add(each);
            }
        }
        return result;
    }
    
    private static ColumnProjectionSegment createColumnProjection(final ColumnProjectionSegment originalColumn, final IdentifierValue subqueryTableName, final TableSourceType tableSourceType) {
        ColumnSegment newColumnSegment = new ColumnSegment(0, 0, originalColumn.getAlias().orElseGet(() -> originalColumn.getColumn().getIdentifier()));
        if (!Strings.isNullOrEmpty(subqueryTableName.getValue())) {
            newColumnSegment.setOwner(new OwnerSegment(0, 0, subqueryTableName));
        }
        ColumnSegmentBoundInfo inputColumnBoundInfo = originalColumn.getColumn().getColumnBoundInfo();
        TableSegmentBoundInfo tableBoundInfo = new TableSegmentBoundInfo(inputColumnBoundInfo.getOriginalDatabase(), inputColumnBoundInfo.getOriginalSchema());
        TableSourceType columnTableSourceType = TableSourceType.MIXED_TABLE == tableSourceType ? getTableSourceTypeFromInputColumn(inputColumnBoundInfo) : tableSourceType;
        newColumnSegment.setColumnBoundInfo(new ColumnSegmentBoundInfo(tableBoundInfo, inputColumnBoundInfo.getOriginalTable(), inputColumnBoundInfo.getOriginalColumn(), columnTableSourceType));
        ColumnProjectionSegment result = new ColumnProjectionSegment(newColumnSegment);
        result.setVisible(originalColumn.isVisible());
        return result;
    }
    
    private static ColumnProjectionSegment createColumnProjection(final ExpressionSegment expressionSegment, final IdentifierValue subqueryTableName, final DatabaseType databaseType) {
        ColumnSegment newColumnSegment = new ColumnSegment(0, 0,
                new IdentifierValue(getColumnNameFromExpression(expressionSegment, databaseType), new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getQuoteCharacter()));
        if (!Strings.isNullOrEmpty(subqueryTableName.getValue())) {
            newColumnSegment.setOwner(new OwnerSegment(0, 0, subqueryTableName));
        }
        ColumnProjectionSegment result = new ColumnProjectionSegment(newColumnSegment);
        result.setVisible(true);
        return result;
    }
    
    private static TableSourceType getTableSourceTypeFromInputColumn(final ColumnSegmentBoundInfo inputColumnBoundInfo) {
        return null == inputColumnBoundInfo ? TableSourceType.TEMPORARY_TABLE : inputColumnBoundInfo.getTableSourceType();
    }
    
    private static String getColumnNameFromExpression(final ExpressionSegment expressionSegment, final DatabaseType databaseType) {
        ProjectionIdentifierExtractEngine extractEngine = new ProjectionIdentifierExtractEngine(databaseType);
        if (expressionSegment instanceof AliasAvailable && ((AliasAvailable) expressionSegment).getAlias().isPresent()) {
            return extractEngine.getIdentifierValue(((AliasAvailable) expressionSegment).getAlias().get());
        }
        return extractEngine.getColumnNameFromExpression(expressionSegment);
    }
}
