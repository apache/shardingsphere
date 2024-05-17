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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.match.MatchAgainstExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.operator.common.SQLExtensionOperatorTable;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.ExpressionConverter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Match expression converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MatchExpressionConverter {
    
    /**
     * Convert match against expression to sql node.
     * 
     * @param segment match against expression
     * @return sql node
     */
    public static Optional<SqlNode> convert(final MatchAgainstExpression segment) {
        List<SqlNode> sqlNodes = new LinkedList<>();
        List<String> names = new ArrayList<>();
        for (ColumnSegment each : segment.getColumns()) {
            if (each.getOwner().isPresent()) {
                addOwnerNames(names, each.getOwner().get());
            }
            names.add(each.getIdentifier().getValue());
        }
        sqlNodes.add(new SqlIdentifier(names, SqlParserPos.ZERO));
        ExpressionConverter.convert(segment.getExpr()).ifPresent(sqlNodes::add);
        SqlNode searchModifier = SqlLiteral.createCharString(segment.getSearchModifier(), SqlParserPos.ZERO);
        sqlNodes.add(searchModifier);
        return Optional.of(new SqlBasicCall(SQLExtensionOperatorTable.MATCH_AGAINST, sqlNodes, SqlParserPos.ZERO));
    }
    
    private static void addOwnerNames(final List<String> names, final OwnerSegment owner) {
        if (null != owner) {
            addOwnerNames(names, owner.getOwner().orElse(null));
            names.add(owner.getIdentifier().getValue());
        }
    }
}
