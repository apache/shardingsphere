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
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public final void assertInit() throws SQLException {
        AbstractBootstrapInitializer initializer = mock(AbstractBootstrapInitializer.class, CALLS_REAL_METHODS);
        ShardingSphereProxy shardingSphereProxy = mock(ShardingSphereProxy.class);
        setFieldValue(initializer, "shardingSphereProxy", shardingSphereProxy);
        DistMetaDataPersistService distMetaDataPersistService = mock(DistMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        when(distMetaDataPersistService.getSchemaMetaDataService().loadAllNames()).thenReturn(Collections.emptyList());
        setFieldValue(initializer, "distMetaDataPersistService", distMetaDataPersistService);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        ConfigurationProperties props = mock(ConfigurationProperties.class);
        when(props.getValue(ConfigurationPropertyKey.XA_TRANSACTION_MANAGER_TYPE)).thenReturn("Atomikos");
        when(metaDataContexts.getProps()).thenReturn(props);
        doReturn(metaDataContexts).when(initializer).decorateMetaDataContexts(any());
        doReturn(mock(TransactionContexts.class)).when(initializer).decorateTransactionContexts(any(), any());
        YamlProxyConfiguration yamlConfig = mock(YamlProxyConfiguration.class, RETURNS_DEEP_STUBS);
        when(yamlConfig.getRuleConfigurations()).thenReturn(Collections.emptyMap());
        when(yamlConfig.getServerConfiguration()).thenReturn(mock(YamlProxyServerConfiguration.class));
        when(yamlConfig.getServerConfiguration().getProps()).thenReturn(new Properties());
        initializer.init(yamlConfig, eq(anyInt()));
        verify(shardingSphereProxy).start(anyInt());
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setFieldValue(final AbstractBootstrapInitializer initializer, final String fieldName, final Object fieldValue) {
        Field field = AbstractBootstrapInitializer.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(initializer, fieldValue);
    }
}
