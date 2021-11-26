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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.projection.impl;

import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.statement.SelectStatementConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Subquery projection converter. 
 */
public final class SubqueryProjectionConverter implements SQLSegmentConverter<SubqueryProjectionSegment, SqlNode> {
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    @Override
    public Optional<SqlNode> convertToSQLNode(final SubqueryProjectionSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        SqlNode sqlNode = new SelectStatementConverter().convertToSQLNode(segment.getSubquery().getSelect());
        return segment.getAlias().isPresent() ? convertToSQLStatement(sqlNode, segment.getAlias().get()) : Optional.of(sqlNode);
    }
    
    private Optional<SqlNode> convertToSQLStatement(final SqlNode sqlNode, final String alias) {
        Collection<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(sqlNode);
        sqlNodes.add(new SqlIdentifier(alias, SqlParserPos.ZERO));
        return Optional.of(new SqlBasicCall(SqlStdOperatorTable.AS, sqlNodes.toArray(new SqlNode[]{}), SqlParserPos.ZERO));
    }
    
    @Override
    public Optional<SubqueryProjectionSegment> convertToSQLSegment(final SqlNode sqlNode) {
        if (sqlNode instanceof SqlSelect || sqlNode instanceof SqlOrderBy) {
            SelectStatement selectStatement = new SelectStatementConverter().convertToSQLStatement(sqlNode);
            // FIXME subquery projection position returned by the CalCite parser does not contain two brackets
            int startIndex = getStartIndex(sqlNode) - 1;
            int stopIndex = getStopIndex(sqlNode) + 1;
            String text = "(" + sqlNode + ")";
            String originalText = text.replace(LINE_SEPARATOR, " ").replace(Quoting.BACK_TICK.string, "");
            return Optional.of(new SubqueryProjectionSegment(new SubquerySegment(startIndex, stopIndex, selectStatement), originalText));
        }
        return Optional.empty();
    }
}
