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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl;

import org.apache.shardingsphere.distsql.parser.statement.rdl.RuleDefinitionStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.AddResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.AlterResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.DropResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.RuleDefinitionBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class RDLBackendHandlerFactoryTest {
    
    @Test
    public void assertNewInstanceWithAddResourceStatement() throws SQLException {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(AddResourceStatement.class), mock(ConnectionSession.class)), instanceOf(AddResourceBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithAlterResourceStatement() throws SQLException {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(AlterResourceStatement.class), mock(ConnectionSession.class)), instanceOf(AlterResourceBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithDropResourceStatement() throws SQLException {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(DropResourceStatement.class), mock(ConnectionSession.class)), instanceOf(DropResourceBackendHandler.class));
    }
    
    @Test
    public void assertNewInstanceWithRuleDefinitionStatement() throws SQLException {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(RuleDefinitionStatement.class), mock(ConnectionSession.class)), instanceOf(RuleDefinitionBackendHandler.class));
    }
}
