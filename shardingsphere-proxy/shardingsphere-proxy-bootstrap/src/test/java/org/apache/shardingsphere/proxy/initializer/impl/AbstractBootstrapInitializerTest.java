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
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
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
        AbstractBootstrapInitializer initializer = mock(AbstractBootstrapInitializer.class);
        setDistMetaDataPersistService(initializer);
        MetaDataContexts metaDataContexts = mockMetaDataContexts();
        when(initializer.decorateMetaDataContexts(any())).thenReturn(metaDataContexts);
        initializer.init(new YamlProxyConfiguration(new YamlProxyServerConfiguration(), Collections.emptyMap()));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setDistMetaDataPersistService(final AbstractBootstrapInitializer initializer) {
        Field field = AbstractBootstrapInitializer.class.getDeclaredField("distMetaDataPersistService");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(initializer, mockDistMetaDataPersistService());
    }
    
    private DistMetaDataPersistService mockDistMetaDataPersistService() {
        DistMetaDataPersistService result = mock(DistMetaDataPersistService.class, RETURNS_DEEP_STUBS);
        when(result.getSchemaMetaDataService().loadAllNames()).thenReturn(Collections.emptyList());
        return result;
    }
    
    private MetaDataContexts mockMetaDataContexts() {
        MetaDataContexts result = mock(MetaDataContexts.class);
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.XA_TRANSACTION_MANAGER_TYPE.getKey(), "Atomikos");
        when(result.getProps()).thenReturn(new ConfigurationProperties(props));
        return result;
    }
}
