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

package org.apache.shardingsphere.sql.parser.engine.oracle.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class OracleDDLStatementVisitorTest {
    
    private static final CacheOption CACHE_OPTION = new CacheOption(128, 1024L);
    
    @Test
    void assertVisitCreateViewWithExplicitColumnNames() {
        CreateViewStatement actual = (CreateViewStatement) parseStatement(
                "CREATE OR REPLACE FORCE VIEW \"V_USER\" (\"ID\", \"PASSWORD\", \"STATUS\") AS SELECT ID, PASSWORD, STATUS FROM T_USER");
        assertThat(actual.getColumns().size(), is(3));
        assertThat(actual.getColumns().get(0).getColumn().getIdentifier().getValue(), is("ID"));
        assertThat(actual.getColumns().get(1).getColumn().getIdentifier().getValue(), is("PASSWORD"));
        assertThat(actual.getColumns().get(2).getColumn().getIdentifier().getValue(), is("STATUS"));
    }
    
    private Object parseStatement(final String sql) {
        return new SQLStatementVisitorEngine("Oracle").visit(new SQLParserEngine("Oracle", CACHE_OPTION).parse(sql, false));
    }
}
