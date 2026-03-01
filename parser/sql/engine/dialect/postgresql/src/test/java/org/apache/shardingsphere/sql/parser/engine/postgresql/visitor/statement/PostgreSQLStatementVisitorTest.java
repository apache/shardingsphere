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

package org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSQLStatementVisitorTest {
    
    @Test
    void assertVisitUnaryNotWithScalarSubquery() {
        String sql = "select 1 from t17 as ref_0 where not ((ref_0.c59) < (select ref_1.c36 as c_0 from t23 as ref_1 join t22 as ref_2 "
                + "on(ref_1.colocated_key = ref_2.colocated_key)))";
        ParseASTNode parseASTNode = new SQLParserEngine("PostgreSQL", new CacheOption(128, 1024L)).parse(sql, false);
        SelectStatement statement = (SelectStatement) new SQLStatementVisitorEngine("PostgreSQL").visit(parseASTNode);
        assertTrue(statement.getWhere().isPresent());
        assertThat(statement.getWhere().get().getExpr(), Matchers.instanceOf(NotExpression.class));
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromSelect(statement);
        Collection<String> rewriteTableNames = tableExtractor.getRewriteTables().stream()
                .map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toList());
        assertThat(rewriteTableNames, Matchers.hasItems("t17", "t23", "t22"));
    }
}
