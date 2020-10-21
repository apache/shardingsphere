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

package org.apache.shardingsphere.sql.parser.cache;

import org.apache.shardingsphere.sql.parser.engine.statement.standard.StandardSQLParsedResultCache;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class StandardSQLParsedResultCacheTest {
    
    @Test
    public void assertGetSQLStatementWithinCache() {
        StandardSQLParsedResultCache actual = new StandardSQLParsedResultCache();
        SQLStatement selectStatement = new MySQLSelectStatement();
        actual.put("SELECT 1", selectStatement);
        assertTrue(actual.get("SELECT 1").isPresent());
        assertThat(actual.get("SELECT 1").get(), is(selectStatement));
    }
    
    @Test
    public void assertGetSQLStatementWithoutCache() {
        StandardSQLParsedResultCache actual = new StandardSQLParsedResultCache();
        SQLStatement selectStatement = new MySQLSelectStatement();
        actual.put("SELECT 1", selectStatement);
        assertFalse(actual.get("SELECT 2").isPresent());
    }
    
    @Test
    public void assertClear() {
        StandardSQLParsedResultCache actual = new StandardSQLParsedResultCache();
        SQLStatement selectStatement = new MySQLSelectStatement();
        actual.put("SELECT 1", selectStatement);
        actual.clear();
        assertFalse(actual.get("SELECT 1").isPresent());
    }
}
