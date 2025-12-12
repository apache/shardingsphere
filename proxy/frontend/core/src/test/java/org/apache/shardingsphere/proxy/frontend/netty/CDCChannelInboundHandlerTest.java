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
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Builder;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Type;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.LoginRequestBody.BasicBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Status;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CDCChannelInboundHandlerTest {
    
    private final EmbeddedChannel channel = new EmbeddedChannel(new LoggingHandler(), new CDCChannelInboundHandler());
    
    @BeforeEach
    void setup() {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(mockAuthorityRule()));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        return result;
    }
    
    private AuthorityRule mockAuthorityRule() {
        AuthorityRule result = mock(AuthorityRule.class);
        when(result.findUser(any())).thenReturn(Optional.of(new ShardingSphereUser("root", "root", "%")));
        return result;
    }
    
    @Test
    void assertLoginRequestFailed() {
        CDCRequest actualRequest = CDCRequest.newBuilder().setType(Type.LOGIN).setLoginRequestBody(LoginRequestBody.newBuilder().setBasicBody(BasicBody.newBuilder().setUsername("root2").build())
                .build()).build();
        channel.writeInbound(actualRequest);
        CDCResponse expectedGreetingResult = channel.readOutbound();
        assertTrue(expectedGreetingResult.hasServerGreetingResult());
        CDCResponse expectedLoginResult = channel.readOutbound();
        assertThat(expectedLoginResult.getStatus(), is(Status.FAILED));
        assertThat(expectedLoginResult.getErrorCode(), is(XOpenSQLState.DATA_SOURCE_REJECTED_CONNECTION_ATTEMPT.getValue()));
        assertFalse(channel.isOpen());
    }
    
    @Test
    void assertIllegalLoginRequest() {
        CDCRequest actualRequest = CDCRequest.newBuilder().setType(Type.LOGIN).setVersion(1).setRequestId("test").build();
        channel.writeInbound(actualRequest);
        CDCResponse expectedGreetingResult = channel.readOutbound();
        assertTrue(expectedGreetingResult.hasServerGreetingResult());
        CDCResponse expectedLoginResult = channel.readOutbound();
        assertThat(expectedLoginResult.getStatus(), is(Status.FAILED));
        assertThat(expectedLoginResult.getErrorCode(), is(XOpenSQLState.NOT_FOUND.getValue()));
        assertFalse(channel.isOpen());
    }
    
    @Test
    void assertLoginRequestSucceed() {
        String encryptPassword = Hashing.sha256().hashBytes("root".getBytes()).toString().toUpperCase();
        Builder builder = CDCRequest.newBuilder().setType(Type.LOGIN).setLoginRequestBody(LoginRequestBody.newBuilder().setBasicBody(BasicBody.newBuilder().setUsername("root")
                .setPassword(encryptPassword).build()).build());
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
