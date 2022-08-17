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

package org.apache.shardingsphere.data.pipeline.core;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.fixture.MigrationJobAPIFixture;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPIFactory;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.junit.Test;
import org.mockito.MockedStatic;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class MigrationJobAPIFactoryTest {
    
    @Test
    public void assertGetInstance() {
        ContextManager contextManager = mock(ContextManager.class);
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        MetaDataPersistService metaDataPersistService = mock(MetaDataPersistService.class);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(metaDataContexts.getPersistService()).thenReturn(metaDataPersistService);
        try (MockedStatic<PipelineContext> pipelineContext = mockStatic(PipelineContext.class)) {
            pipelineContext.when(PipelineContext::getContextManager).thenReturn(contextManager);
            assertThat(MigrationJobAPIFactory.getInstance(), instanceOf(MigrationJobAPIFixture.class));
        }
    }
}
