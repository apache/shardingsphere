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

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.context.metadata.GovernanceMetaDataContexts;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.proxy.fixture.FixtureRegistryCenterRepository;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ClusterBootstrapInitializerTest extends AbstractBootstrapInitializerTest {
    
    private final FixtureRegistryCenterRepository registryCenterRepository = new FixtureRegistryCenterRepository();
    
    @Test
    public void assertDecorateMetaDataContexts() {
        StandardMetaDataContexts metaDataContexts = mock(StandardMetaDataContexts.class);
        when(metaDataContexts.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        MetaDataContexts actualMetaDataContexts = getInitializer().decorateMetaDataContexts(metaDataContexts);
        assertNotNull(actualMetaDataContexts);
        assertThat(actualMetaDataContexts, instanceOf(GovernanceMetaDataContexts.class));
        assertThat(actualMetaDataContexts.getMetaData(DefaultSchema.LOGIC_NAME), is(metaDataContexts.getMetaData(DefaultSchema.LOGIC_NAME)));
        assertThat(actualMetaDataContexts.getProps(), is(metaDataContexts.getProps()));
    }
    
    @Override
    protected void prepareSpecifiedInitializer() {
        ShardingSphereMode mode = mock(ShardingSphereMode.class);
        when(mode.getPersistRepository()).thenReturn(Optional.of(registryCenterRepository));
        ClusterBootstrapInitializer initializer = new ClusterBootstrapInitializer(mode, false);
        setDistMetaDataPersistService(initializer);
        setInitializer(initializer);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setDistMetaDataPersistService(final ClusterBootstrapInitializer initializer) {
        Field field = AbstractBootstrapInitializer.class.getDeclaredField("distMetaDataPersistService");
        field.setAccessible(true);
        field.set(initializer, new DistMetaDataPersistService(registryCenterRepository));
    }
}
