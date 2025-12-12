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

package org.apache.shardingsphere.proxy.frontend.ssl;

import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ProxySSLContextTest {
    
    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        Plugins.getMemberAccessor().set(ProxySSLContext.class.getDeclaredField("sslContext"), ProxySSLContext.getInstance(), null);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(mock(ContextManager.class, RETURNS_DEEP_STUBS));
    }
    
    @Test
    void assertInitWithSSLNotEnabled() throws SSLException {
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_FRONTEND_SSL_ENABLED)).thenReturn(false);
        ProxySSLContext.init();
        assertNull(getSslContext());
    }
    
    @Test
    void assertInitWithGeneratedSelfSignedCertificate() throws SSLException {
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_FRONTEND_SSL_ENABLED)).thenReturn(true);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<String>getValue(ConfigurationPropertyKey.PROXY_FRONTEND_SSL_VERSION))
                .thenReturn("TLSv1.2,TLSv1.3");
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<String>getValue(ConfigurationPropertyKey.PROXY_FRONTEND_SSL_CIPHER))
                .thenReturn("CIPHER1,CIPHER2");
        SslContextBuilder builder = mock(SslContextBuilder.class);
        SslContext expectedSslContext = mock(SslContext.class);
        when(builder.build()).thenReturn(expectedSslContext);
        SSLEngine expectedSSLEngine = mock(SSLEngine.class);
        when(expectedSslContext.newEngine(UnpooledByteBufAllocator.DEFAULT)).thenReturn(expectedSSLEngine);
        try (MockedStatic<SslContextBuilder> mockedStatic = mockStatic(SslContextBuilder.class)) {
            mockedStatic.when(() -> SslContextBuilder.forServer(any(PrivateKey.class), any(X509Certificate.class))).thenReturn(builder);
            ProxySSLContext.init();
        }
        assertThat(getSslContext(), is(expectedSslContext));
        assertTrue(ProxySSLContext.getInstance().isSSLEnabled());
        assertThat(ProxySSLContext.getInstance().newSSLEngine(UnpooledByteBufAllocator.DEFAULT), is(expectedSSLEngine));
    }
    
    @SneakyThrows({NoSuchFieldException.class, IllegalAccessException.class})
    private SslContext getSslContext() {
        return (SslContext) Plugins.getMemberAccessor().get(ProxySSLContext.class.getDeclaredField("sslContext"), ProxySSLContext.getInstance());
    }
    
    @AfterEach
    void tearDown() throws NoSuchFieldException, IllegalAccessException {
        Plugins.getMemberAccessor().set(ProxySSLContext.class.getDeclaredField("sslContext"), ProxySSLContext.getInstance(), null);
    }
}
