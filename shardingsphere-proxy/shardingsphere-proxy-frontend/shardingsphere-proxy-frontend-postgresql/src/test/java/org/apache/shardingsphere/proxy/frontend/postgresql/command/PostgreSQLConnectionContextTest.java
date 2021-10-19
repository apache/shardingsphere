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

package org.apache.shardingsphere.proxy.frontend.postgresql.command;

import lombok.SneakyThrows;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.PostgreSQLPortal;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class PostgreSQLConnectionContextTest {
    
    @Test
    public void assertCreatePortal() throws SQLException {
        PostgreSQLConnectionContext context = new PostgreSQLConnectionContext();
        PostgreSQLBinaryStatement statement = mock(PostgreSQLBinaryStatement.class);
        when(statement.getSql()).thenReturn("");
        when(statement.getSqlStatement()).thenReturn(new EmptyStatement());
        PostgreSQLPortal actual = context.createPortal("P_1", statement, Collections.emptyList(), Collections.emptyList(), mock(BackendConnection.class));
        assertThat(actual, is(getPortals(context).get("P_1")));
    }
    
    @Test
    public void assertGetPortal() {
        PostgreSQLConnectionContext actual = new PostgreSQLConnectionContext();
        Map<String, PostgreSQLPortal> portals = getPortals(actual);
        PostgreSQLPortal expected = mock(PostgreSQLPortal.class);
        portals.put("P_1", expected);
        assertThat(actual.getPortal("P_1"), is(expected));
    }
    
    @Test
    public void assertClosePortal() throws SQLException {
        PostgreSQLConnectionContext actual = new PostgreSQLConnectionContext();
        Map<String, PostgreSQLPortal> portals = getPortals(actual);
        PostgreSQLPortal actualPortal = mock(PostgreSQLPortal.class);
        portals.put("P_1", actualPortal);
        actual.closePortal("P_1");
        verify(actualPortal).close();
        assertFalse(portals.containsKey("P_1"));
    }
    
    @Test
    public void assertCloseAllPortals() throws SQLException {
        PostgreSQLConnectionContext actual = new PostgreSQLConnectionContext();
        Map<String, PostgreSQLPortal> portals = getPortals(actual);
        PostgreSQLPortal portal1 = mock(PostgreSQLPortal.class);
        PostgreSQLPortal portal2 = mock(PostgreSQLPortal.class);
        portals.put("P_1", portal1);
        portals.put("P_2", portal2);
        actual.closeAllPortals();
        verify(portal1).close();
        verify(portal2).close();
        assertTrue(portals.isEmpty());
    }
    
    @Test(expected = SQLException.class)
    public void assertCloseAllPortalsOccursException() throws SQLException {
        PostgreSQLConnectionContext actual = new PostgreSQLConnectionContext();
        Map<String, PostgreSQLPortal> portals = getPortals(actual);
        PostgreSQLPortal portal = mock(PostgreSQLPortal.class);
        doThrow(mock(SQLException.class)).when(portal).close();
        portals.put("P_1", portal);
        actual.closeAllPortals();
    }
    
    @Test
    public void assertClearContext() {
        PostgreSQLConnectionContext actual = new PostgreSQLConnectionContext();
        actual.setErrorOccurred(true);
        actual.setCurrentPacketType(mock(PostgreSQLCommandPacketType.class));
        actual.clearContext();
        assertNull(actual.getCurrentPacketType());
        assertFalse(actual.isErrorOccurred());
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private Map<String, PostgreSQLPortal> getPortals(final PostgreSQLConnectionContext target) {
        Field portalsField = PostgreSQLConnectionContext.class.getDeclaredField("portals");
        portalsField.setAccessible(true);
        return (Map<String, PostgreSQLPortal>) portalsField.get(target);
    }
}
