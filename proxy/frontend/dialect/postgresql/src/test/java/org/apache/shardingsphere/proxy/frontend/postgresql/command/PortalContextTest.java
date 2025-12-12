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

import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.Portal;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PortalContextTest {
    
    private final PortalContext portalContext = new PortalContext();
    
    @Test
    void assertAddAndGetUnnamedPortal() throws SQLException {
        assertAddAndGetPortal("");
    }
    
    @Test
    void assertAddAndGetNamedPortal() throws SQLException {
        assertAddAndGetPortal("P_1");
    }
    
    private void assertAddAndGetPortal(final String portalName) throws SQLException {
        Portal portal = mock(Portal.class);
        when(portal.getName()).thenReturn(portalName);
        portalContext.add(portal);
        assertThat(portalContext.get(portalName), is(portal));
    }
    
    @Test
    void assertAddDuplicateNamedPortal() throws SQLException {
        Portal portal = mock(Portal.class);
        when(portal.getName()).thenReturn("P_1");
        portalContext.add(portal);
        assertThrows(IllegalStateException.class, () -> portalContext.add(portal));
    }
    
    @Test
    void assertCloseSinglePortal() throws SQLException {
        Portal portal = mock(Portal.class);
        String portalName = "P_1";
        when(portal.getName()).thenReturn(portalName);
        portalContext.add(portal);
        portalContext.close(portalName);
        verify(portal).close();
    }
    
    @Test
    void assertCloseAllPortals() throws SQLException {
        Portal portal1 = mock(Portal.class);
        when(portal1.getName()).thenReturn("P_1");
        Portal portal2 = mock(Portal.class);
        when(portal2.getName()).thenReturn("P_2");
        portalContext.add(portal1);
        portalContext.add(portal2);
        portalContext.closeAll();
        verify(portal1).close();
        verify(portal2).close();
    }
}
