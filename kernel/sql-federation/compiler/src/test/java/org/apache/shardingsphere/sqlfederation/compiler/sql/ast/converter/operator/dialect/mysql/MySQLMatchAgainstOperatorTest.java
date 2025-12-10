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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.operator.dialect.mysql;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSyntax;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.pretty.SqlPrettyWriter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MySQLMatchAgainstOperatorTest {
    
    @Test
    void assertUnparseWithModifier() {
        List<SqlNode> operands = Arrays.asList(
                new SqlIdentifier("col1", SqlParserPos.ZERO),
                new SqlIdentifier("col2", SqlParserPos.ZERO),
                SqlLiteral.createCharString("search", SqlParserPos.ZERO),
                SqlLiteral.createCharString("IN BOOLEAN MODE", SqlParserPos.ZERO));
        SqlPrettyWriter writer = new SqlPrettyWriter();
        MySQLMatchAgainstOperator operator = new MySQLMatchAgainstOperator();
        operator.unparse(writer, new SqlBasicCall(operator, operands, SqlParserPos.ZERO), 0, 0);
        assertThat(writer.toSqlString().getSql(), is("MATCH (\"col1\", \"col2\") AGAINST ('search' IN BOOLEAN MODE)"));
        assertTrue(operator.getOperandCountRange().isValidCount(operands.size()));
        assertThat(operator.getSyntax(), is(SqlSyntax.SPECIAL));
    }
}
