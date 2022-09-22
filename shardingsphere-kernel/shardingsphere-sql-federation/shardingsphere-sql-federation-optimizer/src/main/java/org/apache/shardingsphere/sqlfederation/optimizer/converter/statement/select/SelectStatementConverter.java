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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.statement.select;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.groupby.GroupByConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.groupby.HavingConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.limit.PaginationValueSQLConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.orderby.OrderByConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.projection.DistinctConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.projection.ProjectionsConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.where.WhereConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.statement.SQLStatementConverter;

import java.util.Optional;

/**
 * Select statement converter.
 */
public final class SelectStatementConverter implements SQLStatementConverter<SelectStatement, SqlNode> {
    
    @Override
    public SqlNode convert(final SelectStatement selectStatement) {
        SqlNodeList distinct = new DistinctConverter().convert(selectStatement.getProjections()).orElse(null);
        SqlNodeList projection = new ProjectionsConverter().convert(selectStatement.getProjections()).orElseThrow(IllegalStateException::new);
        SqlNode from = new TableConverter().convert(selectStatement.getFrom()).orElse(null);
        SqlNode where = selectStatement.getWhere().flatMap(optional -> new WhereConverter().convert(optional)).orElse(null);
        SqlNodeList groupBy = selectStatement.getGroupBy().flatMap(optional -> new GroupByConverter().convert(optional)).orElse(null);
        SqlNode having = selectStatement.getHaving().flatMap(optional -> new HavingConverter().convert(optional)).orElse(null);
        SqlNodeList orderBy = selectStatement.getOrderBy().flatMap(optional -> new OrderByConverter().convert(optional)).orElse(SqlNodeList.EMPTY);
        Optional<LimitSegment> limit = SelectStatementHandler.getLimitSegment(selectStatement);
        SqlSelect sqlSelect = new SqlSelect(SqlParserPos.ZERO, distinct, projection, from, where, groupBy, having, SqlNodeList.EMPTY, null, null, null, SqlNodeList.EMPTY);
        if (limit.isPresent()) {
            SqlNode offset = limit.get().getOffset().flatMap(optional -> new PaginationValueSQLConverter().convert(optional)).orElse(null);
            SqlNode rowCount = limit.get().getRowCount().flatMap(optional -> new PaginationValueSQLConverter().convert(optional)).orElse(null);
            return new SqlOrderBy(SqlParserPos.ZERO, sqlSelect, orderBy, offset, rowCount);
        }
        return !orderBy.isEmpty() ? new SqlOrderBy(SqlParserPos.ZERO, sqlSelect, orderBy, null, null) : sqlSelect;
    }
}
