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

import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.JDBCPortal;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.Portal;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class PostgreSQLConnectionContextTest {
    
    private PostgreSQLConnectionContext connectionContext;
    
    @Before
    public void setup() {
        connectionContext = new PostgreSQLConnectionContext();
    }
    
    @Test
    public void assertAddAndGetUnnamedPortal() {
        assertAddAndGetPortal("");
    }
    
    @Test
    public void assertAddAndGetNamedPortal() {
        assertAddAndGetPortal("P_1");
    }
    
    private void assertAddAndGetPortal(final String portalName) {
        Portal<?> portal = mock(Portal.class);
        when(portal.getName()).thenReturn(portalName);
        connectionContext.addPortal(portal);
        assertThat(connectionContext.getPortal(portalName), is(portal));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertAddDuplicateNamedPortal() {
        Portal<?> portal = mock(Portal.class);
        when(portal.getName()).thenReturn("P_1");
        connectionContext.addPortal(portal);
        connectionContext.addPortal(portal);
    }
    
    @Test
    public void assertCloseSinglePortal() {
        Portal<?> portal = mock(Portal.class);
        String portalName = "P_1";
        when(portal.getName()).thenReturn(portalName);
        connectionContext.addPortal(portal);
        connectionContext.closePortal(portalName);
        verify(portal).close();
    }
    
    @Test
    public void assertCloseAllPortals() {
        Portal<?> portal1 = mock(JDBCPortal.class);
        when(portal1.getName()).thenReturn("P_1");
        Portal<?> portal2 = mock(JDBCPortal.class);
        when(portal2.getName()).thenReturn("P_2");
        connectionContext.addPortal(portal1);
        connectionContext.addPortal(portal2);
        connectionContext.closeAllPortals();
        verify(portal1).close();
        verify(portal2).close();
    }
}
