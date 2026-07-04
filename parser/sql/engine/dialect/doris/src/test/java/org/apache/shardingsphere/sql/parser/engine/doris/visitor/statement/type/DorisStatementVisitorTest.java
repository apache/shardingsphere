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

package org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DorisStatementVisitorTest {
    
    private static final CacheOption CACHE_OPTION = new CacheOption(128, 1024L);
    
    @Test
    void assertVisitCreateTemporaryTable() {
        CreateTableStatement statement = (CreateTableStatement) parse("CREATE TEMPORARY TABLE t_order (order_id INT)");
        assertTrue(statement.isTemporary());
    }
    
    @Test
    void assertVisitCreateTableWithoutTemporary() {
        CreateTableStatement statement = (CreateTableStatement) parse("CREATE TABLE t_order (order_id INT)");
        assertFalse(statement.isTemporary());
    }
    
    @Test
    void assertVisitDropTemporaryTable() {
        DropTableStatement statement = (DropTableStatement) parse("DROP TEMPORARY TABLE t_order");
        assertTrue(statement.isTemporary());
    }
    
    @Test
    void assertVisitDropTableWithoutTemporary() {
        DropTableStatement statement = (DropTableStatement) parse("DROP TABLE t_order");
        assertFalse(statement.isTemporary());
    }
    
    private Object parse(final String sql) {
        return new SQLStatementVisitorEngine("Doris").visit(new SQLParserEngine("Doris", CACHE_OPTION).parse(sql, false));
    }
}
