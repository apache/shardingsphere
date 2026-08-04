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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OracleDDLStatementVisitorTest {
    
    private static final CacheOption CACHE_OPTION = new CacheOption(128, 1024L);
    
    @Test
    void assertVisitCreateTableAsSelect() {
        CreateTableStatement actual = parse("CREATE TABLE t_order_new AS SELECT * FROM t_order");
        assertThat(actual.getTable().getTableName().getIdentifier().getValue(), is("t_order_new"));
        assertTrue(actual.getColumns().isEmpty());
        assertTrue(actual.getSelectStatement().isPresent());
    }
    
    @Test
    void assertVisitCreateTableAsSelectWithExplicitColumnNames() {
        CreateTableStatement actual = parse("CREATE TABLE t_order_new (order_id_new, user_id_new) AS SELECT order_id, user_id FROM t_order");
        assertThat(actual.getTable().getTableName().getIdentifier().getValue(), is("t_order_new"));
        assertThat(actual.getColumns().size(), is(2));
        assertThat(actual.getColumns().get(0).getIdentifier().getValue(), is("order_id_new"));
        assertThat(actual.getColumns().get(1).getIdentifier().getValue(), is("user_id_new"));
        assertTrue(actual.getSelectStatement().isPresent());
    }
    
    private CreateTableStatement parse(final String sql) {
        return (CreateTableStatement) new SQLStatementVisitorEngine("Oracle").visit(new SQLParserEngine("Oracle", CACHE_OPTION).parse(sql, false));
    }
}
