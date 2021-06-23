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

package org.apache.shardingsphere.infra.optimize.core.convert.converter.impl;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.core.convert.converter.SqlNodeConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;

import java.util.Optional;

/**
 * Select statement sql node converter.
 */
public final class SelectStatementSqlNodeConverter implements SqlNodeConverter<SelectStatement, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final SelectStatement selectStatement) {
        Optional<SqlNodeList> distinct = new DistinctSqlNodeConverter().convert(selectStatement.getProjections());
        Optional<SqlNodeList> projections = new ProjectionsSqlNodeConverter().convert(selectStatement.getProjections());
        Optional<SqlNode> from = new TableSqlNodeConverter().convert(selectStatement.getFrom());
        Optional<SqlNode> where = new WhereSqlNodeConverter().convert(selectStatement.getWhere().orElse(null));
        Optional<SqlNodeList> groupBy = new GroupBySqlNodeConverter().convert(selectStatement.getGroupBy().orElse(null));
        Optional<SqlNode> having = new HavingSqlNodeConverter().convert(selectStatement.getHaving().orElse(null));
        Optional<SqlNodeList> orderBy = new OrderBySqlNodeConverter().convert(selectStatement.getOrderBy().orElse(null));
        Optional<LimitSegment> limit = SelectStatementHandler.getLimitSegment(selectStatement);
        Optional<SqlNode> offset = new OffsetSqlNodeConverter().convert(limit.orElse(null));
        Optional<SqlNode> rowCount = new RowCountSqlNodeConverter().convert(limit.orElse(null));
        return Optional.of(new SqlSelect(SqlParserPos.ZERO, 
                distinct.orElse(null), 
                projections.orElse(null), 
                from.orElse(null), 
                where.orElse(null), 
                groupBy.orElse(null), 
                having.orElse(null),
                null, 
                orderBy.orElse(null),
                offset.orElse(null),
                rowCount.orElse(null), 
                null));
    }
}
