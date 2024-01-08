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

package org.apache.shardingsphere.data.pipeline.cdc.context;

import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.stubbing.Answer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class CDCJobItemContextTest {
    
    @Test
    void assertJobItemContextGet() {
        CDCJobConfiguration mockConfig = mock(CDCJobConfiguration.class);
        when(mockConfig.getJobId()).thenReturn("mock_job_id");
        TransmissionJobItemProgress mockProcess = mock(TransmissionJobItemProgress.class);
        when(mockProcess.getProcessedRecordsCount()).thenReturn(5L);
        when(mockProcess.getInventoryRecordsCount()).thenReturn(10L);
        CDCJobItemContext cdcJobItemContext = new CDCJobItemContext(mockConfig, 1, mockProcess, mock(TransmissionProcessContext.class), mock(CDCTaskConfiguration.class),
                mock(PipelineDataSourceManager.class), mock(PipelineSink.class));
        assertThat(cdcJobItemContext.getJobId(), is("mock_job_id"));
        assertThat(cdcJobItemContext.getProcessedRecordsCount(), is(5L));
        assertThat(cdcJobItemContext.getInventoryRecordsCount(), is(10L));
    }
    
    @Test
    void assertUpdateCount() {
        CDCJobItemContext cdcJobItemContext = new CDCJobItemContext(mock(CDCJobConfiguration.class), 1, null, mock(TransmissionProcessContext.class), mock(CDCTaskConfiguration.class),
                mock(PipelineDataSourceManager.class), mock(PipelineSink.class));
        assertThat(cdcJobItemContext.getInventoryRecordsCount(), is(0L));
        cdcJobItemContext.updateInventoryRecordsCount(1L);
        assertThat(cdcJobItemContext.getInventoryRecordsCount(), is(1L));
        assertThat(cdcJobItemContext.getProcessedRecordsCount(), is(0L));
        try (MockedStatic<PipelineJobProgressPersistService> mockStatic = mockStatic(PipelineJobProgressPersistService.class)) {
            mockStatic.when(() -> PipelineJobProgressPersistService.notifyPersist(anyString(), anyInt())).thenAnswer((Answer<Void>) invocation -> null);
            cdcJobItemContext.onProgressUpdated(new PipelineJobProgressUpdatedParameter(10));
            assertThat(cdcJobItemContext.getProcessedRecordsCount(), is(10L));
        }
    }
}
