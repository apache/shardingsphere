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

package org.apache.shardingsphere.proxy.frontend.netty;

import com.google.common.hash.Hashing;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.data.pipeline.cdc.common.CDCResponseErrorCode;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Builder;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequest.BasicBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Status;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class CDCChannelInboundHandlerTest {
    
    private static MockedStatic<ProxyContext> proxyContext;
    
    private final CDCChannelInboundHandler cdcChannelInboundHandler = new CDCChannelInboundHandler();
    
    private EmbeddedChannel channel;
    
    @BeforeClass
    public static void beforeClass() {
        proxyContext = mockStatic(ProxyContext.class);
        ProxyContext mockedProxyContext = mock(ProxyContext.class, RETURNS_DEEP_STUBS);
        proxyContext.when(ProxyContext::getInstance).thenReturn(mockedProxyContext);
        AuthorityRule authorityRule = mock(AuthorityRule.class);
        ShardingSphereUser rootUser = new ShardingSphereUser("root", "root", "%");
        when(authorityRule.findUser(any())).thenReturn(Optional.of(rootUser));
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = new ShardingSphereRuleMetaData(Collections.singletonList(authorityRule));
        when(mockedProxyContext.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(shardingSphereRuleMetaData);
    }
    
    @AfterClass
    public static void afterClass() {
        proxyContext.close();
    }
    
    @Before
    public void setup() {
        channel = new EmbeddedChannel(new LoggingHandler(), cdcChannelInboundHandler);
    }
    
    @Test
    public void assertLoginRequestFailed() {
        CDCRequest actualRequest = CDCRequest.newBuilder().setLogin(LoginRequest.newBuilder().setBasicBody(BasicBody.newBuilder().setUsername("root2").build()).build()).build();
        channel.writeInbound(actualRequest);
        CDCResponse expectedGreetingResult = channel.readOutbound();
        assertTrue(expectedGreetingResult.hasServerGreetingResult());
        CDCResponse expectedLoginResult = channel.readOutbound();
        assertThat(expectedLoginResult.getStatus(), is(Status.FAILED));
        assertThat(expectedLoginResult.getErrorCode(), is(CDCResponseErrorCode.ILLEGAL_USERNAME_OR_PASSWORD.getCode()));
        assertFalse(channel.isOpen());
    }
    
    @Test
    public void assertIllegalLoginRequest() {
        CDCRequest actualRequest = CDCRequest.newBuilder().setVersion(1).setRequestId("test").build();
        channel.writeInbound(actualRequest);
        CDCResponse expectedGreetingResult = channel.readOutbound();
        assertTrue(expectedGreetingResult.hasServerGreetingResult());
        CDCResponse expectedLoginResult = channel.readOutbound();
        assertThat(expectedLoginResult.getStatus(), is(Status.FAILED));
        assertThat(expectedLoginResult.getErrorCode(), is(CDCResponseErrorCode.ILLEGAL_REQUEST_ERROR.getCode()));
        assertFalse(channel.isOpen());
    }
    
    @Test
    public void assertLoginRequestSucceed() {
        String encryptPassword = Hashing.sha256().hashBytes("root".getBytes()).toString().toUpperCase();
        Builder builder = CDCRequest.newBuilder().setLogin(LoginRequest.newBuilder().setBasicBody(BasicBody.newBuilder().setUsername("root").setPassword(encryptPassword).build()).build());
        CDCRequest actualRequest = builder.build();
        channel.writeInbound(actualRequest);
        CDCResponse expectedGreetingResult = channel.readOutbound();
        assertTrue(expectedGreetingResult.hasServerGreetingResult());
        CDCResponse expectedLoginResult = channel.readOutbound();
        assertThat(expectedLoginResult.getStatus(), is(Status.SUCCEED));
        assertThat(expectedLoginResult.getErrorCode(), is(""));
        assertThat(expectedLoginResult.getErrorMessage(), is(""));
    }
}
