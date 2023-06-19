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
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.UnregisterStorageUnitStatement;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.rule.RuleDefinitionBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.storage.unit.AlterStorageUnitBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.storage.unit.RegisterStorageUnitBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.storage.unit.UnregisterStorageUnitBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class RDLBackendHandlerFactoryTest {
    
    @Test
    void assertNewInstanceWithRegisterStorageUnitStatement() {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(RegisterStorageUnitStatement.class), mock(ConnectionSession.class)), instanceOf(RegisterStorageUnitBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithAlterStorageUnitStatement() {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(AlterStorageUnitStatement.class), mock(ConnectionSession.class)), instanceOf(AlterStorageUnitBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWithUnregisterStorageUnitStatement() {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(UnregisterStorageUnitStatement.class), mock(ConnectionSession.class)), instanceOf(UnregisterStorageUnitBackendHandler.class));
    }
    
    // TODO
    @Disabled("enable this when metadata structure adjustment completed")
    @Test
    void assertNewInstanceWithRuleDefinitionStatement() {
        assertThat(RDLBackendHandlerFactory.newInstance(mock(RuleDefinitionStatement.class), mock(ConnectionSession.class)), instanceOf(RuleDefinitionBackendHandler.class));
    }
}
