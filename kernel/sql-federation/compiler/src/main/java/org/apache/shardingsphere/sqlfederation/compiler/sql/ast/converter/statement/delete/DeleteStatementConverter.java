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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.delete;

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlDelete;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.limit.PaginationValueSQLConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.orderby.OrderByConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.where.WhereConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.with.WithConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.SQLStatementConverter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Delete statement converter.
 */
public final class DeleteStatementConverter implements SQLStatementConverter<DeleteStatement, SqlNode> {
    
    @Override
    public SqlNode convert(final DeleteStatement deleteStatement) {
        SqlNode sqlDelete = convertDelete(deleteStatement);
        SqlNodeList orderBy = deleteStatement.getOrderBy().flatMap(OrderByConverter::convert).orElse(SqlNodeList.EMPTY);
        Optional<LimitSegment> limit = deleteStatement.getLimit();
        if (limit.isPresent()) {
            SqlNode offset = limit.get().getOffset().flatMap(PaginationValueSQLConverter::convert).orElse(null);
            SqlNode rowCount = limit.get().getRowCount().flatMap(PaginationValueSQLConverter::convert).orElse(null);
            return new SqlOrderBy(SqlParserPos.ZERO, sqlDelete, orderBy, offset, rowCount);
        }
        return orderBy.isEmpty() ? sqlDelete : new SqlOrderBy(SqlParserPos.ZERO, sqlDelete, orderBy, null, null);
    }
    
    private SqlNode convertDelete(final DeleteStatement deleteStatement) {
        if (deleteStatement.getTable() instanceof DeleteMultiTableSegment) {
            return convertDeleteMultipleTable(deleteStatement);
        } else {
            return convertDeleteSingleTable(deleteStatement);
        }
    }
    
    private SqlNode convertDeleteMultipleTable(final DeleteStatement deleteStatement) {
        DeleteMultiTableSegment multiTableSegment = (DeleteMultiTableSegment) deleteStatement.getTable();
        SqlNodeList targetTables = convertTargetTables(multiTableSegment);
        SqlNodeList targetTableAliases = convertTargetTableAliases(multiTableSegment);
        SqlNode condition = deleteStatement.getWhere().flatMap(WhereConverter::convert).orElse(null);
        SqlDelete sqlDelete = new SqlDelete(SqlParserPos.ZERO, targetTables.get(0), condition, null,
                targetTableAliases.isEmpty() ? null : (SqlIdentifier) targetTableAliases.get(0));
        return deleteStatement.getWith().flatMap(optional -> WithConverter.convert(optional, sqlDelete)).orElse(sqlDelete);
    }
    
    private SqlNodeList convertTargetTables(final DeleteMultiTableSegment multiTableSegment) {
        Collection<SqlNode> sqlNodes = new LinkedList<>();
        Map<String, SimpleTableSegment> aliasSimpleTables = buildAliasSQLNodes(multiTableSegment.getRelationTable());
        for (SimpleTableSegment each : multiTableSegment.getActualDeleteTables()) {
            String tableName = each.getTableName().getIdentifier().getValue();
            SimpleTableSegment tableSegment = !each.getOwner().isPresent() && aliasSimpleTables.containsKey(tableName) ? aliasSimpleTables.get(tableName) : each;
            TableConverter.convert(tableSegment).ifPresent(sqlNodes::add);
        }
        return new SqlNodeList(sqlNodes, SqlParserPos.ZERO);
    }
    
    private Map<String, SimpleTableSegment> buildAliasSQLNodes(final TableSegment tableSegment) {
        Map<String, SimpleTableSegment> result = new CaseInsensitiveMap<>();
        if (tableSegment instanceof SimpleTableSegment && tableSegment.getAliasName().isPresent()) {
            result.put(tableSegment.getAliasName().get(), new SimpleTableSegment(((SimpleTableSegment) tableSegment).getTableName()));
        }
        if (tableSegment instanceof JoinTableSegment) {
            result.putAll(buildAliasSQLNodes(((JoinTableSegment) tableSegment).getLeft()));
            result.putAll(buildAliasSQLNodes(((JoinTableSegment) tableSegment).getRight()));
        }
        return result;
    }
    
    private SqlNodeList convertTargetTableAliases(final DeleteMultiTableSegment multiTableSegment) {
        Collection<SqlNode> sqlNodes = new LinkedList<>();
        multiTableSegment.getActualDeleteTables().forEach(each -> sqlNodes.add(new SqlIdentifier(each.getTableName().getIdentifier().getValue(), SqlParserPos.ZERO)));
        return new SqlNodeList(sqlNodes, SqlParserPos.ZERO);
    }
    
    private SqlNode convertDeleteSingleTable(final DeleteStatement deleteStatement) {
        SqlNode deleteTable = TableConverter.convert(deleteStatement.getTable()).orElseThrow(IllegalStateException::new);
        SqlNode condition = deleteStatement.getWhere().flatMap(WhereConverter::convert).orElse(null);
        SqlIdentifier alias = deleteStatement.getTable().getAliasName().map(optional -> new SqlIdentifier(optional, SqlParserPos.ZERO)).orElse(null);
        SqlDelete sqlDelete = new SqlDelete(SqlParserPos.ZERO, getTargetTableName(deleteTable), condition, null, alias);
        return deleteStatement.getWith().flatMap(optional -> WithConverter.convert(optional, sqlDelete)).orElse(sqlDelete);
    }
    
    private SqlNode getTargetTableName(final SqlNode deleteTable) {
        return deleteTable instanceof SqlBasicCall ? ((SqlBasicCall) deleteTable).getOperandList().iterator().next() : deleteTable;
    }
}
