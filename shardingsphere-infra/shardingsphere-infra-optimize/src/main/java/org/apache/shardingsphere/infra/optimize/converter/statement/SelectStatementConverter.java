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

package org.apache.shardingsphere.infra.optimize.converter.statement;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.converter.context.ConverterContext;
import org.apache.shardingsphere.infra.optimize.converter.segment.from.TableConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.groupby.GroupByConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.groupby.HavingConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.limit.PaginationValueSQLConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.orderby.OrderByConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.projection.DistinctConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.projection.ProjectionsConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.where.WhereConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Select statement converter.
 */
public final class SelectStatementConverter implements SQLStatementConverter<SelectStatement, SqlNode> {
    
    private static final int LIMIT_SEGMENT_LENGTH = 6;
    
    @Override
    public SqlNode convertToSQLNode(final SelectStatement selectStatement) {
        SqlNodeList distinct = new DistinctConverter().convertToSQLNode(selectStatement.getProjections()).orElse(null);
        SqlNodeList projection = new ProjectionsConverter().convertToSQLNode(selectStatement.getProjections()).orElseThrow(IllegalStateException::new);
        SqlNode from = new TableConverter().convertToSQLNode(selectStatement.getFrom()).orElse(null);
        SqlNode where = selectStatement.getWhere().flatMap(optional -> new WhereConverter().convertToSQLNode(optional)).orElse(null);
        SqlNodeList groupBy = selectStatement.getGroupBy().flatMap(optional -> new GroupByConverter().convertToSQLNode(optional)).orElse(null);
        SqlNode having = selectStatement.getHaving().flatMap(optional -> new HavingConverter().convertToSQLNode(optional)).orElse(null);
        SqlNodeList orderBy = selectStatement.getOrderBy().flatMap(optional -> new OrderByConverter().convertToSQLNode(optional)).orElse(SqlNodeList.EMPTY);
        Optional<LimitSegment> limit = SelectStatementHandler.getLimitSegment(selectStatement);
        ConverterContext context = new ConverterContext();
        SqlSelect sqlSelect = new SqlSelect(SqlParserPos.ZERO, distinct, projection, from,
                where, groupBy, having, SqlNodeList.EMPTY, null, null, null, SqlNodeList.EMPTY);
        if (limit.isPresent()) {
            SqlNode offset = limit.get().getOffset().flatMap(optional -> new PaginationValueSQLConverter(context).convertToSQLNode(optional)).orElse(null);
            SqlNode rowCount = limit.get().getRowCount().flatMap(optional -> new PaginationValueSQLConverter(context).convertToSQLNode(optional)).orElse(null);
            return new SqlOrderBy(SqlParserPos.ZERO, sqlSelect, orderBy, offset, rowCount);
        }
        return !orderBy.isEmpty() ? new SqlOrderBy(SqlParserPos.ZERO, sqlSelect, orderBy, null, null) : sqlSelect;
    }
    
    @Override
    public SelectStatement convertToSQLStatement(final SqlNode sqlNode) {
        SqlSelect sqlSelect = sqlNode instanceof SqlOrderBy ? (SqlSelect) ((SqlOrderBy) sqlNode).query : (SqlSelect) sqlNode;
        ProjectionsSegment projections = new ProjectionsConverter().convertToSQLSegment(sqlSelect.getSelectList()).orElseThrow(IllegalStateException::new);
        projections.setDistinctRow(sqlSelect.isDistinct());
        // TODO create select statement for different dialect 
        MySQLSelectStatement result = new MySQLSelectStatement();
        result.setProjections(projections);
        new TableConverter().convertToSQLSegment(sqlSelect.getFrom()).ifPresent(result::setFrom);
        new WhereConverter().convertToSQLSegment(sqlSelect.getWhere()).ifPresent(result::setWhere);
        new GroupByConverter().convertToSQLSegment(sqlSelect.getGroup()).ifPresent(result::setGroupBy);
        new HavingConverter().convertToSQLSegment(sqlSelect.getHaving()).ifPresent(result::setHaving);
        ConverterContext context = new ConverterContext();
        if (sqlNode instanceof SqlOrderBy) {
            SqlOrderBy sqlOrderBy = (SqlOrderBy) sqlNode;
            new OrderByConverter().convertToSQLSegment(sqlOrderBy.orderList).ifPresent(result::setOrderBy);
            createLimitSegment(sqlOrderBy, context).ifPresent(result::setLimit);
        }
        result.setParameterCount(context.getParameterCount().get());
        return result;
    }
    
    private Optional<LimitSegment> createLimitSegment(final SqlOrderBy sqlOrderBy, final ConverterContext context) {
        if (null == sqlOrderBy.offset && null == sqlOrderBy.fetch) {
            return Optional.empty();
        }
        Optional<PaginationValueSegment> offset = Optional.ofNullable(sqlOrderBy.offset).flatMap(optional -> new PaginationValueSQLConverter(context).convertToSQLSegment(optional));
        Optional<PaginationValueSegment> rowCount = Optional.ofNullable(sqlOrderBy.fetch).flatMap(optional -> new PaginationValueSQLConverter(context).convertToSQLSegment(optional));
        List<Integer> startIndexes = new LinkedList<>();
        List<Integer> stopIndexes = new LinkedList<>();
        offset.map(SQLSegment::getStartIndex).ifPresent(startIndexes::add);
        rowCount.map(SQLSegment::getStartIndex).ifPresent(startIndexes::add);
        offset.map(SQLSegment::getStopIndex).ifPresent(stopIndexes::add);
        rowCount.map(SQLSegment::getStopIndex).ifPresent(stopIndexes::add);
        // FIXME Now sqlNode position returned by the CalCite parser does not contain LIMIT and requires manual calculation
        int startIndex = startIndexes.stream().min(Comparator.naturalOrder()).orElse(0) - LIMIT_SEGMENT_LENGTH;
        int stopIndex = stopIndexes.stream().max(Comparator.naturalOrder()).orElse(0);
        return Optional.of(new LimitSegment(startIndex, stopIndex, offset.orElse(null), rowCount.orElse(null)));
    }
}
