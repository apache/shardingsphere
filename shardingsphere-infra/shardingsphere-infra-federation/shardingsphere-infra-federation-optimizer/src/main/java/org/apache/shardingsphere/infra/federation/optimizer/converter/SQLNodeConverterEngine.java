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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.statement.select.SelectStatementConverter;
import org.apache.shardingsphere.infra.federation.optimizer.converter.type.CombineOperatorConverter;
import org.apache.shardingsphere.sql.parser.sql.common.constant.CombineType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

/**
 * SQL node converter engine.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLNodeConverterEngine {
    
    /**
     * Convert SQL statement to SQL node.
     * 
     * @param statement SQL statement to be converted
     * @return sqlNode converted SQL node
     */
    public static SqlNode convertToSQLNode(final SQLStatement statement) {
        if (statement instanceof SelectStatement) {
            SqlNode sqlNode = new SelectStatementConverter().convertToSQLNode((SelectStatement) statement);
            for (CombineSegment each : ((SelectStatement) statement).getCombines()) {
                SqlNode combineSqlNode = convertToSQLNode(each.getSelectStatement());
                return new SqlBasicCall(CombineOperatorConverter.convert(each.getCombineType()), new SqlNode[]{sqlNode, combineSqlNode}, SqlParserPos.ZERO);
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
            CombineType combineType = CombineOperatorConverter.convert(((SqlBasicCall) sqlNode).getOperator());
            int startIndex = rightSqlNode.getParserPosition().getColumnNum() - (((SqlBasicCall) sqlNode).getOperator().getName() + " ").length() - 1;
            int stopIndex = rightSqlNode.getParserPosition().getEndColumnNum() - 1;
            leftSelectStatement.getCombines().add(new CombineSegment(startIndex, stopIndex, combineType, rightSelectStatement));
            return leftSelectStatement;
        }
        throw new UnsupportedOperationException("Unsupported SQL statement conversion.");
    }
}
