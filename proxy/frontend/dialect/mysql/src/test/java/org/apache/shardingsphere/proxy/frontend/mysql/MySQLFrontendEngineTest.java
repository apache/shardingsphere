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

package org.apache.shardingsphere.proxy.frontend.mysql;

import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeMap;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.database.protocol.mysql.netty.MySQLSequenceIdInboundHandler;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.mysql.connection.MySQLConnectionIdRegistry;
import org.apache.shardingsphere.proxy.frontend.netty.FrontendChannelInboundHandler;
import org.apache.shardingsphere.proxy.frontend.netty.FrontendConstants;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, MySQLStatementIdGenerator.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLFrontendEngineTest {
    
    private MySQLFrontendEngine engine;
    
    private final MySQLConnectionIdRegistry connectionRegistry = MySQLConnectionIdRegistry.getInstance();
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Channel channel;
    
    @BeforeEach
    void setUp() {
        engine = new MySQLFrontendEngine();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up singleton state after tests
        connectionRegistry.unregister(1L);
    }
    
    @Test
    void assertInitChannel() {
        engine.initChannel(channel);
        verify(channel.attr(MySQLConstants.SEQUENCE_ID_ATTRIBUTE_KEY)).set(any(AtomicInteger.class));
        verify(channel.pipeline())
                .addBefore(eq(FrontendChannelInboundHandler.class.getSimpleName()), eq(MySQLSequenceIdInboundHandler.class.getSimpleName()), isA(MySQLSequenceIdInboundHandler.class));
    }
    
    @Test
    void assertRelease() {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        AttributeMap attributeMap = mock(AttributeMap.class);
        Attribute<Long> attribute = mock(Attribute.class);
        
        when(connectionSession.getConnectionId()).thenReturn(1);
        when(connectionSession.getAttributeMap()).thenReturn(attributeMap);
        when(attributeMap.attr(FrontendConstants.NATIVE_CONNECTION_ID_ATTRIBUTE_KEY))
                .thenReturn(attribute);
        
        engine.release(connectionSession);
        
        verify(MySQLStatementIdGenerator.getInstance()).unregisterConnection(1);
    }
    
    @Test
    void assertReleaseUnregistersConnectionIdMapping() {
        int mysqlConnectionId = 1;
        String processId = "test_process_123";
        
        // registry precondition
        connectionRegistry.register(mysqlConnectionId, processId);
        assertThat(connectionRegistry.getProcessId(mysqlConnectionId), is(processId));
        
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        AttributeMap attributeMap = mock(AttributeMap.class);
        Attribute<Long> attribute = mock(Attribute.class);
        
        when(connectionSession.getConnectionId()).thenReturn(mysqlConnectionId);
        when(connectionSession.getAttributeMap()).thenReturn(attributeMap);
        when(attributeMap.attr(FrontendConstants.NATIVE_CONNECTION_ID_ATTRIBUTE_KEY))
                .thenReturn(attribute);
        when(attribute.get()).thenReturn((long) mysqlConnectionId);
        
        engine.release(connectionSession);
        
        assertThat(connectionRegistry.getProcessId(mysqlConnectionId), nullValue());
    }
    
}
