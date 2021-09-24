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

package org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl;

import com.google.common.base.Preconditions;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.infra.optimize.operator.BinarySQLOperator;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;

import java.util.Optional;

/**
 * Binary operation expression converter.
 */
public final class BinaryOperationExpressionConverter implements SQLSegmentConverter<BinaryOperationExpression, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final BinaryOperationExpression segment) {
        BinarySQLOperator operator = BinarySQLOperator.value(segment.getOperator());
        SqlNode left = convertExpression(segment.getLeft());
        SqlNode right = convertExpression(segment.getRight());
        return Optional.of(new SqlBasicCall(operator.getSqlBinaryOperator(), new SqlNode[] {left, right}, SqlParserPos.ZERO));
    }
    
    private SqlNode convertExpression(final ExpressionSegment segment) {
        Optional<SqlNode> result = new ExpressionConverter().convert(segment);
        Preconditions.checkState(result.isPresent());
        return result.get();
    }
}
