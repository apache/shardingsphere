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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.from.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.statement.SelectStatementConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Subquery table converter.
 */
public final class SubqueryTableConverter implements SQLSegmentConverter<SubqueryTableSegment, SqlBasicCall> {
    
    @Override
    public Optional<SqlBasicCall> convertToSQLNode(final SubqueryTableSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        Collection<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(new SelectStatementConverter().convertToSQLNode(segment.getSubquery().getSelect()));
        segment.getAlias().ifPresent(optional -> sqlNodes.add(new SqlIdentifier(optional, SqlParserPos.ZERO)));
        return Optional.of(new SqlBasicCall(SqlStdOperatorTable.AS, sqlNodes.toArray(new SqlNode[]{}), SqlParserPos.ZERO));
    }
    
    @Override
    public Optional<SubqueryTableSegment> convertToSQLSegment(final SqlBasicCall sqlBasicCall) {
        SqlNode select = sqlBasicCall.getOperandList().get(0);
        SelectStatement selectStatement = new SelectStatementConverter().convertToSQLStatement(select);
        SubqueryTableSegment result = new SubqueryTableSegment(new SubquerySegment(getStartIndex(sqlBasicCall), getStopIndex(sqlBasicCall), selectStatement));
        if (sqlBasicCall.getOperator().equals(SqlStdOperatorTable.AS)) {
            SqlNode alias = sqlBasicCall.getOperandList().get(1);
            result.setAlias(new AliasSegment(getStartIndex(alias), getStopIndex(alias), new IdentifierValue(alias.toString())));   
        }
        return Optional.of(result);
    }
}
