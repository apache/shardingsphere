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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl;

import lombok.RequiredArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.statement.select.SelectStatementConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;

import java.util.Collections;
import java.util.Optional;

/**
 * Exists subquery expression converter.
 */
@RequiredArgsConstructor
public final class ExistsSubqueryExpressionConverter implements SQLSegmentConverter<ExistsSubqueryExpression, SqlBasicCall> {
    
    private final boolean not;
    
    public ExistsSubqueryExpressionConverter() {
        not = false;
    }
    
    @Override
    public Optional<SqlBasicCall> convert(final ExistsSubqueryExpression expression) {
        if (null == expression) {
            return Optional.empty();
        }
        SqlBasicCall sqlNode = new SqlBasicCall(SqlStdOperatorTable.EXISTS,
                Collections.singletonList(new SelectStatementConverter().convert(expression.getSubquery().getSelect())), SqlParserPos.ZERO);
        return expression.isNot() ? Optional.of(new SqlBasicCall(SqlStdOperatorTable.NOT, Collections.singletonList(sqlNode), SqlParserPos.ZERO)) : Optional.of(sqlNode);
    }
}
