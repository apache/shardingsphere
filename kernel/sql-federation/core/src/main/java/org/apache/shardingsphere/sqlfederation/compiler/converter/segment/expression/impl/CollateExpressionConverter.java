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

package org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sqlfederation.compiler.converter.function.dialect.mysql.SQLExtensionOperatorTable;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.ExpressionConverter;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Collate expression converter.
 */
public final class CollateExpressionConverter implements SQLSegmentConverter<CollateExpression, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final CollateExpression segment) {
        List<SqlNode> sqlNodes = new LinkedList<>();
        sqlNodes.add(segment.getExpr().flatMap(optional -> new ExpressionConverter().convert(optional)).orElse(SqlNodeList.EMPTY));
        sqlNodes.add(new ExpressionConverter().convert(segment.getCollateName()).orElse(SqlNodeList.EMPTY));
        return Optional.of(new SqlBasicCall(SQLExtensionOperatorTable.COLLATE, sqlNodes, SqlParserPos.ZERO));
    }
}
