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

package org.apache.shardingsphere.sqlfederation.compiler.converter.statement.delete;

import org.apache.calcite.sql.SqlDelete;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.DeleteStatementHandler;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.limit.PaginationValueSQLConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.orderby.OrderByConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.where.WhereConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.statement.SQLStatementConverter;

import java.util.Optional;

/**
 * Delete statement converter.
 */
public final class DeleteStatementConverter implements SQLStatementConverter<DeleteStatement, SqlNode> {
    
    @Override
    public SqlNode convert(final DeleteStatement deleteStatement) {
        SqlDelete sqlDelete = convertDelete(deleteStatement);
        SqlNodeList orderBy = DeleteStatementHandler.getOrderBySegment(deleteStatement).flatMap(optional -> new OrderByConverter().convert(optional)).orElse(SqlNodeList.EMPTY);
        Optional<LimitSegment> limit = DeleteStatementHandler.getLimitSegment(deleteStatement);
        if (limit.isPresent()) {
            SqlNode offset = limit.get().getOffset().flatMap(optional -> new PaginationValueSQLConverter().convert(optional)).orElse(null);
            SqlNode rowCount = limit.get().getRowCount().flatMap(optional -> new PaginationValueSQLConverter().convert(optional)).orElse(null);
            return new SqlOrderBy(SqlParserPos.ZERO, sqlDelete, orderBy, offset, rowCount);
        }
        return orderBy.isEmpty() ? sqlDelete : new SqlOrderBy(SqlParserPos.ZERO, sqlDelete, orderBy, null, null);
    }
    
    private SqlDelete convertDelete(final DeleteStatement deleteStatement) {
        SqlNode deleteTable = new TableConverter().convert(deleteStatement.getTable()).orElseThrow(IllegalStateException::new);
        SqlNode condition = deleteStatement.getWhere().flatMap(optional -> new WhereConverter().convert(optional)).orElse(null);
        SqlIdentifier alias = deleteStatement.getTable().getAliasName().map(optional -> new SqlIdentifier(optional, SqlParserPos.ZERO)).orElse(null);
        return new SqlDelete(SqlParserPos.ZERO, deleteTable, condition, null, alias);
    }
}
