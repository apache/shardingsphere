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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.variable.sqlmode;

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.RequiredSessionVariableRecorder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLSessionSQLModeTest {
    
    @AfterEach
    void resetGlobalSQLMode() {
        MySQLSessionSQLMode.setGlobalValue(MySQLSessionSQLMode.DEFAULT_SQL_MODE);
    }
    
    @Test
    void assertGetDefault() {
        MySQLSessionSQLMode actual = MySQLSessionSQLMode.get(new DefaultAttributeMap());
        assertThat(actual.getValue(), is(MySQLSessionSQLMode.DEFAULT_SQL_MODE));
        assertFalse(actual.isNoBackslashEscapes());
    }
    
    @Test
    void assertSetNoBackslashEscapes() {
        DefaultAttributeMap attributeMap = new DefaultAttributeMap();
        MySQLSessionSQLMode.set("STRICT_TRANS_TABLES,NO_BACKSLASH_ESCAPES", attributeMap);
        MySQLSessionSQLMode actual = MySQLSessionSQLMode.get(attributeMap);
        assertThat(actual.getValue(), is("STRICT_TRANS_TABLES,NO_BACKSLASH_ESCAPES"));
        assertTrue(actual.isNoBackslashEscapes());
    }
    
    @Test
    void assertSetDefault() {
        DefaultAttributeMap attributeMap = new DefaultAttributeMap();
        MySQLSessionSQLMode.set("NO_BACKSLASH_ESCAPES", attributeMap);
        MySQLSessionSQLMode.set(MySQLSessionSQLMode.DEFAULT_SQL_MODE, attributeMap);
        assertFalse(MySQLSessionSQLMode.get(attributeMap).isNoBackslashEscapes());
    }
    
    @Test
    void assertInitialize() {
        MySQLSessionSQLMode.setGlobalValue("NO_BACKSLASH_ESCAPES");
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        DefaultAttributeMap attributeMap = new DefaultAttributeMap();
        when(connectionSession.getAttributeMap()).thenReturn(attributeMap);
        RequiredSessionVariableRecorder recorder = new RequiredSessionVariableRecorder();
        when(connectionSession.getRequiredSessionVariableRecorder()).thenReturn(recorder);
        MySQLSessionSQLMode.initialize(connectionSession);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        assertTrue(MySQLSessionSQLMode.get(attributeMap).isNoBackslashEscapes());
        assertThat(recorder.toSetSQLs(databaseType.getType()), is(java.util.Collections.singletonList("SET sql_mode='NO_BACKSLASH_ESCAPES'")));
    }
    
}
