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

package org.apache.shardingsphere.infra.federation.optimizer.converter;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.statement.SelectStatementConverter;
import org.apache.shardingsphere.sql.parser.sql.common.constant.UnionType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.union.UnionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Map;
import java.util.TreeMap;

/**
 * SQL node converter engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLNodeConverterEngine {
    
    private static final Map<UnionType, SqlOperator> REGISTRY = new TreeMap<>();
    
    static {
        registerUnion();
    }
    
    private static void registerUnion() {
        REGISTRY.put(UnionType.UNION_DISTINCT, SqlStdOperatorTable.UNION);
    }
    
    /**
     * Convert SQL statement to SQL node.
     * 
     * @param statement SQL statement to be converted
     * @return sqlNode converted SQL node
     */
    public static SqlNode convertToSQLNode(final SQLStatement statement) {
        if (statement instanceof SelectStatement) {
            SqlNode sqlNode = new SelectStatementConverter().convertToSQLNode((SelectStatement) statement);
            if (null != ((SelectStatement) statement).getUnionSegments()) {
                for (final UnionSegment unionSegment : ((SelectStatement) statement).getUnionSegments()) {
                    SqlNode unionSqlNode = convertToSQLNode(unionSegment.getSelectStatement());
                    return new SqlBasicCall(convertUnionOperator(unionSegment.getUnionType()), new SqlNode[]{sqlNode, unionSqlNode}, SqlParserPos.ZERO);
                }
            }
            return sqlNode;
        }
        throw new UnsupportedOperationException("Unsupported SQL node conversion.");
    }
    
    /**
     * Convert SQL node to SQL statement.
     *
     * @param sqlNode sqlNode converted SQL node
     * @return SQL statement to be converted
     */
    public static SQLStatement convertToSQLStatement(final SqlNode sqlNode) {
        if (sqlNode instanceof SqlOrderBy || sqlNode instanceof SqlSelect) {
            return new SelectStatementConverter().convertToSQLStatement(sqlNode);
        }
        if (sqlNode instanceof SqlBasicCall && null != ((SqlBasicCall) sqlNode).getOperator() && SqlKind.UNION == ((SqlBasicCall) sqlNode).getOperator().getKind()) {
            SqlNode leftSqlNode = ((SqlBasicCall) sqlNode).getOperandList().get(0);
            SqlNode rightSqlNode = ((SqlBasicCall) sqlNode).getOperandList().get(1);
            SelectStatement leftSelectStatement = (SelectStatement) convertToSQLStatement(leftSqlNode);
            SelectStatement rightSelectStatement = (SelectStatement) convertToSQLStatement(rightSqlNode);
            leftSelectStatement.getUnionSegments().add(new UnionSegment(UnionType.UNION_DISTINCT, rightSelectStatement, rightSqlNode.getParserPosition().getColumnNum() - 7, 
                    rightSqlNode.getParserPosition().getEndColumnNum() - 1));
            return leftSelectStatement;
        }
        throw new UnsupportedOperationException("Unsupported SQL statement conversion.");
    }
    
    private static SqlOperator convertUnionOperator(final UnionType unionType) {
        Preconditions.checkState(REGISTRY.containsKey(unionType), "Unsupported unionType: `%s`", unionType);
        return REGISTRY.get(unionType);
    }
}
