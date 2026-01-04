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

package org.apache.shardingsphere.mode.persist;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.persist.mode.ModePersistServiceFacade;
import org.apache.shardingsphere.mode.persist.mode.ModePersistServiceFacadeBuilder;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistServiceFacadeTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContextManager metaDataContextManager;
    
    @Mock
    private PersistRepository repository;
    
    @Mock
    private ModePersistServiceFacadeBuilder builder;
    
    @Mock
    private ModePersistServiceFacade modeFacade;
    
    @Test
    void assertCreateFacade() {
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            PersistServiceFacade actual = createFacade(mockedStatic);
            assertThat(actual.getRepository(), is(repository));
            assertNotNull(actual.getMetaDataFacade());
            assertNotNull(actual.getStateService());
            assertNotNull(actual.getQualifiedDataSourceStateService());
            assertThat(actual.getModeFacade(), is(modeFacade));
            verify(builder).build(metaDataContextManager, repository);
        }
    }
    
    @Test
    void assertCloseChain() {
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            PersistServiceFacade actual = createFacade(mockedStatic);
            actual.close();
            verify(modeFacade).close();
            verify(repository).close();
        }
    }
    
    private PersistServiceFacade createFacade(final MockedStatic<TypedSPILoader> mockedStatic) {
        when(metaDataContextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED)).thenReturn(Boolean.TRUE);
        mockedStatic.when(() -> TypedSPILoader.getService(ModePersistServiceFacadeBuilder.class, "FIXTURE")).thenReturn(builder);
        when(builder.build(metaDataContextManager, repository)).thenReturn(modeFacade);
        return new PersistServiceFacade(repository, new ModeConfiguration("FIXTURE", null), metaDataContextManager);
    }
}
