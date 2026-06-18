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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.type;

import org.apache.calcite.sql.SqlExplain;
import org.apache.calcite.sql.SqlExplainFormat;
import org.apache.calcite.sql.SqlExplainLevel;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.SQLStatementConverter;

import java.util.Optional;

/**
 * Explain statement converter.
 */
public final class ExplainStatementConverter implements SQLStatementConverter<ExplainStatement, SqlNode> {
    
    @Override
    public SqlNode convert(final ExplainStatement explainStatement) {
        return new SqlExplain(SqlParserPos.ZERO, convertSQLStatement(explainStatement), SqlExplainLevel.ALL_ATTRIBUTES.symbol(SqlParserPos.ZERO),
                SqlExplain.Depth.TYPE.symbol(SqlParserPos.ZERO), SqlExplainFormat.TEXT.symbol(SqlParserPos.ZERO), 0);
    }
    
    private SqlNode convertSQLStatement(final ExplainStatement explainStatement) {
        return convertSqlNode(explainStatement.getExplainableSQLStatement()).orElseThrow(IllegalStateException::new);
    }
    
    private Optional<SqlNode> convertSqlNode(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return Optional.of(new SelectStatementConverter().convert((SelectStatement) sqlStatement));
        } else if (sqlStatement instanceof DeleteStatement) {
            return Optional.of(new DeleteStatementConverter().convert((DeleteStatement) sqlStatement));
        } else if (sqlStatement instanceof UpdateStatement) {
            return Optional.of(new UpdateStatementConverter().convert((UpdateStatement) sqlStatement));
        } else if (sqlStatement instanceof InsertStatement) {
            return Optional.of(new InsertStatementConverter().convert((InsertStatement) sqlStatement));
        }
        return Optional.empty();
    }
}
