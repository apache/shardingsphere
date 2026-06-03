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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.type;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.groupby.GroupByConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.groupby.HavingConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.limit.PaginationValueSQLConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.orderby.OrderByConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.DistinctConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.ProjectionsConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.where.WhereConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.window.WindowConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.with.WithConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.SQLStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.type.CombineOperatorConverter;

import java.util.Arrays;
import java.util.Optional;

/**
 * Select statement converter.
 */
public final class SelectStatementConverter implements SQLStatementConverter<SelectStatement, SqlNode> {
    
    @Override
    public SqlNode convert(final SelectStatement selectStatement, final String databaseType) {
        SqlSelect sqlSelect = convertSelect(selectStatement, databaseType);
        SqlNode sqlWith = convertWith(sqlSelect, selectStatement, databaseType);
        SqlNode sqlCombine = convertCombine(null != sqlWith ? sqlWith : sqlSelect, selectStatement, databaseType);
        SqlNodeList orderBy = selectStatement.getOrderBy().flatMap(segment -> OrderByConverter.convert(segment, databaseType)).orElse(SqlNodeList.EMPTY);
        Optional<LimitSegment> limit = selectStatement.getLimit();
        if (limit.isPresent()) {
            SqlNode offset = limit.get().getOffset().flatMap(segment -> PaginationValueSQLConverter.convert(segment, databaseType)).orElse(null);
            SqlNode rowCount = limit.get().getRowCount().flatMap(segment -> PaginationValueSQLConverter.convert(segment, databaseType)).orElse(null);
            return new SqlOrderBy(SqlParserPos.ZERO, sqlCombine, orderBy, offset, rowCount);
        }
        return orderBy.isEmpty() ? sqlCombine : new SqlOrderBy(SqlParserPos.ZERO, sqlCombine, orderBy, null, null);
    }
    
    private SqlNode convertWith(final SqlNode sqlSelect, final SelectStatement selectStatement, final String databaseType) {
        return selectStatement.getWith().flatMap(segment -> WithConverter.convert(segment, sqlSelect, databaseType)).orElse(null);
    }
    
    private SqlSelect convertSelect(final SelectStatement selectStatement, final String databaseType) {
        SqlNodeList distinct = DistinctConverter.convert(selectStatement.getProjections()).orElse(null);
        SqlNodeList projection = ProjectionsConverter.convert(selectStatement.getProjections(), databaseType).orElseThrow(IllegalStateException::new);
        SqlNode from = selectStatement.getFrom().flatMap(segment -> TableConverter.convert(segment, databaseType)).orElse(null);
        SqlNode where = selectStatement.getWhere().flatMap(segment -> WhereConverter.convert(segment, databaseType)).orElse(null);
        SqlNodeList groupBy = selectStatement.getGroupBy().flatMap(segment -> GroupByConverter.convert(segment, databaseType)).orElse(null);
        SqlNode having = selectStatement.getHaving().flatMap(segment -> HavingConverter.convert(segment, databaseType)).orElse(null);
        SqlNodeList window = selectStatement.getWindow().flatMap(segment -> WindowConverter.convert(segment, databaseType)).orElse(SqlNodeList.EMPTY);
        return new SqlSelect(SqlParserPos.ZERO, distinct, projection, from, where, groupBy, having, window, null, null, null, null, SqlNodeList.EMPTY);
    }
    
    private SqlNode convertCombine(final SqlNode sqlNode, final SelectStatement selectStatement, final String databaseType) {
        if (selectStatement.getCombine().isPresent()) {
            CombineSegment combineSegment = selectStatement.getCombine().get();
            return new SqlBasicCall(CombineOperatorConverter.convert(combineSegment.getCombineType()),
                    Arrays.asList(convert(combineSegment.getLeft().getSelect(), databaseType), convert(combineSegment.getRight().getSelect(), databaseType)), SqlParserPos.ZERO);
        }
        return sqlNode;
    }
}
