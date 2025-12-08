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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.Collection;

/**
 * Sharding IN value item.
 * Similar to InsertValue, stores ExpressionSegment directly.
 */
@RequiredArgsConstructor
@Getter
public final class ShardingInValueItem {
    
    private final ExpressionSegment valueExpression;
    
    private final Collection<DataNode> dataNodes;
    
    @Override
    public String toString() {
        if (valueExpression instanceof ParameterMarkerExpressionSegment) {
            ParameterMarkerExpressionSegment segment = (ParameterMarkerExpressionSegment) valueExpression;
            return ParameterMarkerType.QUESTION == segment.getParameterMarkerType() ? "?" : "$" + (segment.getParameterMarkerIndex() + 1);
        }
        if (valueExpression instanceof LiteralExpressionSegment) {
            Object literals = ((LiteralExpressionSegment) valueExpression).getLiterals();
            if (null == literals) {
                return "NULL";
            }
            return literals instanceof String ? "'" + literals + "'" : String.valueOf(literals);
        }
        return valueExpression.getText();
    }
}
