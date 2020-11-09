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

package org.apache.shardingsphere.proxy.initializer.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Ignore
public abstract class AbstractBootstrapInitializerTest {
    
    @Getter
    @Setter
    private AbstractBootstrapInitializer initializer;
    
    @Before
    public final void setUp() {
        doEnvironmentPrepare();
        prepareSpecifiedInitializer();
    }
    
    protected void doEnvironmentPrepare() {
    
    }
    
    protected abstract void prepareSpecifiedInitializer();
    
    @Test
    public final void assertInit() throws NoSuchFieldException, IllegalAccessException, SQLException {
        Field field = AbstractBootstrapInitializer.class.getDeclaredField("shardingSphereProxy");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        AbstractBootstrapInitializer abstractBootstrapInitializer = mock(AbstractBootstrapInitializer.class, Mockito.CALLS_REAL_METHODS);
        ShardingSphereProxy shardingSphereProxy = mock(ShardingSphereProxy.class);
        field.set(abstractBootstrapInitializer, shardingSphereProxy);
        doReturn(mock(ProxyConfiguration.class)).when(abstractBootstrapInitializer).getProxyConfiguration(any());
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        ConfigurationProperties props = mock(ConfigurationProperties.class);
        when(props.getValue(any())).thenReturn(Boolean.FALSE);
        when(metaDataContexts.getProps()).thenReturn(props);
        doReturn(metaDataContexts).when(abstractBootstrapInitializer).decorateMetaDataContexts(any());
        doReturn(mock(TransactionContexts.class)).when(abstractBootstrapInitializer).decorateTransactionContexts(any());
        YamlProxyConfiguration yamlConfig = mock(YamlProxyConfiguration.class);
        abstractBootstrapInitializer.init(yamlConfig, eq(anyInt()));
        verify(shardingSphereProxy).start(anyInt());
    }
    
    protected final void assertProps(final Properties actual) {
        assertThat(actual.getProperty("alpha-1"), is("alpha-A"));
        assertThat(actual.getProperty("beta-2"), is("beta-B"));
    }
}
