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

package org.apache.shardingsphere.infra.exception.external.sql.identifier;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SQLExceptionIdentifierTest {
    
    @Test
    void assertToStringForColumnIdentifier() {
        assertThat(new SQLExceptionIdentifier("foo_db", "foo_tbl", "foo_col").toString(), is("database.table.column: 'foo_db'.'foo_tbl'.'foo_col'"));
    }
    
    @Test
    void assertToStringForTableIdentifier() {
        assertThat(new SQLExceptionIdentifier("foo_db", "foo_tbl").toString(), is("database.table: 'foo_db'.'foo_tbl'"));
    }
    
    @Test
    void assertToStringForDatabaseIdentifier() {
        assertThat(new SQLExceptionIdentifier("foo_db").toString(), is("database: 'foo_db'"));
    }
}
