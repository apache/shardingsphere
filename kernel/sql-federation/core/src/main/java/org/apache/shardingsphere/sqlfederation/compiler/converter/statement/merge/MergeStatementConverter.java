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

package org.apache.shardingsphere.sqlfederation.compiler.converter.statement.merge;

import org.apache.calcite.sql.SqlMerge;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.MergeStatement;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.statement.SQLStatementConverter;

public final class MergeStatementConverter implements SQLStatementConverter<MergeStatement, SqlNode> {
    @Override
    public SqlNode convert(final MergeStatement mergeStatement) {
        SqlNode targetTable = new TableConverter().convert(mergeStatement.getTarget()).orElseThrow(IllegalStateException::new);
        SqlNode condition = new ExpressionConverter().convert(mergeStatement.getExpression().getExpr()).get();
        SqlNode sourceTable = new TableConverter().convert(mergeStatement.getSource()).orElseThrow(IllegalStateException::new);
        return new SqlMerge(SqlParserPos.ZERO, targetTable, condition, sourceTable,  null, null, null, null);
    }
}
