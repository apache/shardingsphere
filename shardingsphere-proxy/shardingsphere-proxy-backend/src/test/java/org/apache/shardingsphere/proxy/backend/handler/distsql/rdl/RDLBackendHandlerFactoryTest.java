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

import lombok.SneakyThrows;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.AddResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.AlterResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.DropResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class RDLBackendHandlerFactoryTest {
    
    @Mock
    private AddResourceStatement addResourceStatement;
    
    @Mock
    private AlterResourceStatement alterResourceStatement;
    
    @Mock
    private DropResourceStatement dropResourceStatement;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    @SneakyThrows
    public void assertRDLBackendHandlerReturnAddResourceBackendHandler() {
        ProxyBackendHandler handler = RDLBackendHandlerFactory.newInstance(addResourceStatement, connectionSession);
        assertThat(handler, instanceOf(AddResourceBackendHandler.class));
    }
    
    @Test
    @SneakyThrows
    public void assertRDLBackendHandlerReturnAlterResourceStatement() {
        ProxyBackendHandler handler = RDLBackendHandlerFactory.newInstance(alterResourceStatement, connectionSession);
        assertThat(handler, instanceOf(AlterResourceBackendHandler.class));
    }
    
    @Test
    @SneakyThrows
    public void assertRDLBackendHandlerReturnDropResourceStatement() {
        ProxyBackendHandler handler = RDLBackendHandlerFactory.newInstance(dropResourceStatement, connectionSession);
        assertThat(handler, instanceOf(DropResourceBackendHandler.class));
    }
    
}
