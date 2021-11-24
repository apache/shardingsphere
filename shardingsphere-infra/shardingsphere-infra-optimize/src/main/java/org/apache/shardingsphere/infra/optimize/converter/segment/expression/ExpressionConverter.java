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

package org.apache.shardingsphere.infra.optimize.converter.segment.expression;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.SqlDynamicParam;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.fun.SqlBetweenOperator;
import org.apache.calcite.sql.fun.SqlInOperator;
import org.apache.calcite.sql.fun.SqlPositionFunction;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.shardingsphere.infra.optimize.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.BetweenExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.BinaryOperationExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.ExistsSubqueryExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.FunctionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.InExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.ListExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.LiteralExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.ParameterMarkerExpressionConverter;
import org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl.SubqueryExpressionConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;

import java.util.Optional;

/**
 * Expression converter.
 */
public final class ExpressionConverter implements SQLSegmentConverter<ExpressionSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convertToSQLNode(final ExpressionSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        if (segment instanceof LiteralExpressionSegment) {
            return new LiteralExpressionConverter().convertToSQLNode((LiteralExpressionSegment) segment);
        } else if (segment instanceof CommonExpressionSegment) {
            // TODO 
            throw new UnsupportedOperationException("unsupported CommonExpressionSegment");
        } else if (segment instanceof ListExpression) {
            return new ListExpressionConverter().convertToSQLNode((ListExpression) segment);
        } else if (segment instanceof BinaryOperationExpression) {
            return new BinaryOperationExpressionConverter().convertToSQLNode((BinaryOperationExpression) segment).map(optional -> optional);
        } else if (segment instanceof ColumnSegment) {
            return new ColumnConverter().convertToSQLNode((ColumnSegment) segment).map(optional -> optional);
        } else if (segment instanceof ExistsSubqueryExpression) {
            return new ExistsSubqueryExpressionConverter().convertToSQLNode((ExistsSubqueryExpression) segment).map(optional -> optional);
        } else if (segment instanceof SubqueryExpressionSegment) {
            return new SubqueryExpressionConverter().convertToSQLNode((SubqueryExpressionSegment) segment);
        } else if (segment instanceof InExpression) {
            return new InExpressionConverter().convertToSQLNode((InExpression) segment).map(optional -> optional);
        } else if (segment instanceof BetweenExpression) {
            return new BetweenExpressionConverter().convertToSQLNode((BetweenExpression) segment).map(optional -> optional);
        } else if (segment instanceof ParameterMarkerExpressionSegment) {
            return new ParameterMarkerExpressionConverter().convertToSQLNode((ParameterMarkerExpressionSegment) segment).map(optional -> optional);
        } else if (segment instanceof FunctionSegment) {
            return new FunctionConverter().convertToSQLNode((FunctionSegment) segment).map(optional -> optional);
        }
        throw new UnsupportedOperationException("unsupported TableSegment type: " + segment.getClass());
    }
    
    @Override
    public Optional<ExpressionSegment> convertToSQLSegment(final SqlNode sqlNode) {
        if (null == sqlNode) {
            return Optional.empty(); 
        }
        if (sqlNode instanceof SqlIdentifier) {
            return new ColumnConverter().convertToSQLSegment((SqlIdentifier) sqlNode).map(optional -> optional);
        }
        if (sqlNode instanceof SqlBasicCall) {
            return convertToSQLSegment((SqlBasicCall) sqlNode, false);
        }
        if (sqlNode instanceof SqlSelect) {
            return new SubqueryExpressionConverter().convertToSQLSegment(sqlNode).map(optional -> optional);
        }
        if (sqlNode instanceof SqlLiteral) {
            return new LiteralExpressionConverter().convertToSQLSegment(sqlNode).map(optional -> optional);
        }
        if (sqlNode instanceof SqlDynamicParam) {
            return new ParameterMarkerExpressionConverter().convertToSQLSegment(sqlNode).map(optional -> optional);
        }
        return Optional.empty();
    }
    
    private Optional<ExpressionSegment> convertToSQLSegment(final SqlBasicCall sqlBasicCall, final boolean not) {
        if (null == sqlBasicCall) {
            return Optional.empty();
        }
        SqlOperator operator = sqlBasicCall.getOperator();
        if (operator.getName().equals(SqlStdOperatorTable.NOT.getName()) && sqlBasicCall.getOperandList().get(0) instanceof SqlBasicCall) {
            return convertToSQLSegment((SqlBasicCall) sqlBasicCall.getOperandList().get(0), true);
        }
        if (operator instanceof SqlInOperator) {
            return new InExpressionConverter(not).convertToSQLSegment(sqlBasicCall).map(optional -> optional);
        }
        if (operator instanceof SqlBetweenOperator) {
            return new BetweenExpressionConverter(not).convertToSQLSegment(sqlBasicCall).map(optional -> optional);
        }
        if (operator.getName().equals(SqlStdOperatorTable.EXISTS.getName())) {
            return new ExistsSubqueryExpressionConverter(not).convertToSQLSegment(sqlBasicCall).map(optional -> optional);
        }
        if (operator instanceof SqlBinaryOperator) {
            return new BinaryOperationExpressionConverter().convertToSQLSegment(sqlBasicCall).map(optional -> optional);
        }
        if (operator instanceof SqlPositionFunction) {
            return new FunctionConverter().convertToSQLSegment(sqlBasicCall).map(optional -> optional);
        }
        return Optional.empty();
    }
}
