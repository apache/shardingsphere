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

package org.apache.shardingsphere.data.pipeline.scenario.migration.api;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineDataSourcePersistService;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrationJobAPITest {
    
    private final PipelineContextKey contextKey = new PipelineContextKey(InstanceType.PROXY);
    
    @Mock
    private PipelineJobManager jobManager;
    
    @Mock
    private PipelineDataSourcePersistService dataSourcePersistService;
    
    private MigrationJobAPI jobAPI;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        jobAPI = new MigrationJobAPI();
        Plugins.getMemberAccessor().set(MigrationJobAPI.class.getDeclaredField("jobManager"), jobAPI, jobManager);
        Plugins.getMemberAccessor().set(MigrationJobAPI.class.getDeclaredField("dataSourcePersistService"), jobAPI, dataSourcePersistService);
    }
    
    @Test
    void assertScheduleThrowsForConflictingSourceTableIdentity() {
        Collection<MigrationSourceTargetEntry> entries = Arrays.asList(
                new MigrationSourceTargetEntry(new DataNode("foo_ds", "foo_schema", "foo_tbl"), "foo_target"),
                new MigrationSourceTargetEntry(new DataNode("foo_ds", "bar_schema", "FOO_TBL"), "bar_target"));
        PipelineInvalidParameterException actual = assertThrows(PipelineInvalidParameterException.class, () -> jobAPI.schedule(contextKey, entries, "foo_db"));
        assertThat(actual.getMessage(), is("There is invalid parameter value. More than one source table with the same table name for foo_ds"));
        verify(dataSourcePersistService, never()).load(any(PipelineContextKey.class), anyString());
        verify(jobManager, never()).start(any());
    }
    
    @Test
    void assertScheduleAcceptsExactDuplicateSourceTargetEntry() {
        MigrationSourceTargetEntry entry = new MigrationSourceTargetEntry(new DataNode("foo_ds", "foo_schema", "foo_tbl"), "foo_target");
        IllegalStateException expected = new IllegalStateException("reached configuration");
        when(dataSourcePersistService.load(contextKey, "MIGRATION")).thenThrow(expected);
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> jobAPI.schedule(contextKey, Arrays.asList(entry, entry), "foo_db"));
        assertThat(actual, is(expected));
        verify(dataSourcePersistService).load(contextKey, "MIGRATION");
        verify(jobManager, never()).start(any());
    }
    
    @Test
    void assertScheduleAcceptsSameTableNameFromDifferentDataSources() {
        Collection<MigrationSourceTargetEntry> entries = Arrays.asList(
                new MigrationSourceTargetEntry(new DataNode("foo_ds", "foo_schema", "foo_tbl"), "foo_target"),
                new MigrationSourceTargetEntry(new DataNode("bar_ds", "bar_schema", "FOO_TBL"), "bar_target"));
        IllegalStateException expected = new IllegalStateException("reached configuration");
        when(dataSourcePersistService.load(contextKey, "MIGRATION")).thenThrow(expected);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> jobAPI.schedule(contextKey, entries, "foo_db"));
        assertThat(actual, is(expected));
        verify(dataSourcePersistService).load(contextKey, "MIGRATION");
        verify(jobManager, never()).start(any());
    }
}
