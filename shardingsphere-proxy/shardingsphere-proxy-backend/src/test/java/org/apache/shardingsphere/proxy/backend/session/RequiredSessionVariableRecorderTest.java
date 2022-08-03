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

package org.apache.shardingsphere.proxy.backend.session;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class RequiredSessionVariableRecorderTest {
    
    @Test
    public void assertRecordMySQLVariables() {
        RequiredSessionVariableRecorder recorder = new RequiredSessionVariableRecorder();
        assertTrue(recorder.isEmpty());
        String databaseType = "MySQL";
        assertTrue(recorder.toSetSQLs(databaseType).isEmpty());
        assertTrue(recorder.toResetSQLs(databaseType).isEmpty());
        recorder.setVariable("sql_mode", "default");
        recorder.setVariable("max_sort_length", "1024");
        assertFalse(recorder.isEmpty());
        assertThat(recorder.toSetSQLs(databaseType), is(Collections.singletonList("SET sql_mode=default,max_sort_length=1024")));
        assertThat(recorder.toResetSQLs(databaseType), is(Collections.singletonList("SET sql_mode=DEFAULT,max_sort_length=DEFAULT")));
        recorder.removeVariablesWithDefaultValue();
        assertThat(recorder.toSetSQLs(databaseType), is(Collections.singletonList("SET max_sort_length=1024")));
        assertThat(recorder.toResetSQLs(databaseType), is(Collections.singletonList("SET max_sort_length=DEFAULT")));
    }
    
    @Test
    public void assertRecordPostgreSQLVariables() {
        RequiredSessionVariableRecorder recorder = new RequiredSessionVariableRecorder();
        assertTrue(recorder.isEmpty());
        String databaseType = "PostgreSQL";
        assertTrue(recorder.toSetSQLs(databaseType).isEmpty());
        assertTrue(recorder.toResetSQLs(databaseType).isEmpty());
        recorder.setVariable("client_encoding", "utf8");
        recorder.setVariable("datestyle", "default");
        assertFalse(recorder.isEmpty());
        assertThat(new HashSet<>(recorder.toSetSQLs(databaseType)), is(new HashSet<>(Arrays.asList("SET client_encoding=utf8", "SET datestyle=default"))));
        assertThat(recorder.toResetSQLs(databaseType), is(Collections.singletonList("RESET ALL")));
        recorder.removeVariablesWithDefaultValue();
        assertThat(recorder.toSetSQLs(databaseType), is(Collections.singletonList("SET client_encoding=utf8")));
        assertThat(recorder.toResetSQLs(databaseType), is(Collections.singletonList("RESET ALL")));
    }
    
    @Test
    public void assertRecordUnsupportedDatabaseType() {
        RequiredSessionVariableRecorder recorder = new RequiredSessionVariableRecorder();
        assertTrue(recorder.isEmpty());
        recorder.setVariable("key", "value");
        assertTrue(recorder.toSetSQLs("unsupported").isEmpty());
        assertTrue(recorder.toResetSQLs("unsupported").isEmpty());
    }
}
