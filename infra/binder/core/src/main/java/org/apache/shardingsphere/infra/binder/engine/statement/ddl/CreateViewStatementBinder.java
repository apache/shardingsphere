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

package org.apache.shardingsphere.infra.binder.engine.statement.ddl;

import com.google.common.collect.LinkedHashMultimap;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.expression.type.ColumnSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.segment.dml.from.type.SimpleTableSegmentBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementCopyUtils;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.syntax.DifferenceInColumnCountOfSelectListAndColumnNameListException;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TableSourceType;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ColumnExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.view.ViewColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Create view statement binder.
 */
public final class CreateViewStatementBinder implements SQLStatementBinder<CreateViewStatement> {
    
    @Override
    public CreateViewStatement bind(final CreateViewStatement sqlStatement, final SQLStatementBinderContext binderContext) {
        SimpleTableSegment boundView = SimpleTableSegmentBinder.bind(sqlStatement.getView(), binderContext, LinkedHashMultimap.create());
        SelectStatement boundSelect = new SelectStatementBinder().bind(sqlStatement.getSelect(), binderContext);
        return copy(sqlStatement, boundView, boundSelect);
    }
    
    private CreateViewStatement copy(final CreateViewStatement sqlStatement, final SimpleTableSegment boundView, final SelectStatement boundSelect) {
        CreateViewStatement result = new CreateViewStatement(sqlStatement.getDatabaseType());
        result.setView(boundView);
        result.setSelect(boundSelect);
        result.setReplaceView(sqlStatement.isReplaceView());
        result.setViewDefinition(sqlStatement.getViewDefinition());
        result.getColumns().addAll(bindColumns(sqlStatement.getColumns(), boundSelect));
        SQLStatementCopyUtils.copyAttributes(sqlStatement, result);
        return result;
    }
    
    private Collection<ViewColumnSegment> bindColumns(final List<ViewColumnSegment> columns, final SelectStatement boundSelect) {
        List<ProjectionSegment> projections = boundSelect.getProjections().getProjections();
        ShardingSpherePreconditions.checkState(columns.isEmpty() || columns.size() == projections.size(), DifferenceInColumnCountOfSelectListAndColumnNameListException::new);
        Collection<ViewColumnSegment> result = new ArrayList<>(columns.size());
        int index = 0;
        for (ViewColumnSegment each : columns) {
            result.add(new ViewColumnSegment(each.getStartIndex(), each.getStopIndex(), bindColumn(each.getColumn(), projections.get(index)), each.getComment().orElse(null)));
            index++;
        }
        return result;
    }
    
    private ColumnSegment bindColumn(final ColumnSegment column, final ProjectionSegment projection) {
        ColumnSegment result = new ColumnSegment(column.getStartIndex(), column.getStopIndex(), column.getIdentifier());
        result.setNestedObjectAttributes(column.getNestedObjectAttributes());
        column.getOwner().ifPresent(result::setOwner);
        result.setVariable(column.isVariable());
        column.getLeftParentheses().ifPresent(result::setLeftParentheses);
        column.getRightParentheses().ifPresent(result::setRightParentheses);
        result.setColumnBoundInfo(ColumnSegmentBinder.createColumnSegmentBoundInfo(column, findInputColumn(projection).orElse(null), TableSourceType.TEMPORARY_TABLE));
        return result;
    }
    
    private Optional<ColumnSegment> findInputColumn(final ProjectionSegment projection) {
        if (projection instanceof ColumnProjectionSegment) {
            return Optional.of(((ColumnProjectionSegment) projection).getColumn());
        }
        if (projection instanceof ExpressionProjectionSegment && null != ((ExpressionProjectionSegment) projection).getExpr()) {
            Collection<ColumnSegment> columns = ColumnExtractor.extract(((ExpressionProjectionSegment) projection).getExpr());
            return 1 == columns.size() ? Optional.of(columns.iterator().next()) : Optional.empty();
        }
        return Optional.empty();
    }
}
