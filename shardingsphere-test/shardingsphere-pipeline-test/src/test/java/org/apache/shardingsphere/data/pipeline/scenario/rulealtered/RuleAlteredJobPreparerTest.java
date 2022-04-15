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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Answers;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RuleAlteredJobPreparerTest {
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @Test(expected = PipelineJobCreationException.class)
    public void assertPrepareFailedOfNoPrimaryKey() {
        new RuleAlteredJobPreparer().prepare(new RuleAlteredJobContext(JobConfigurationBuilder.createJobConfiguration()));
    }
    
    @Test
    public void assertPrepareSuccess() {
        JobConfiguration jobConfiguration = JobConfigurationBuilder.createJobConfiguration();
        RuleAlteredJobContext mockJobContext = new RuleAlteredJobContext(jobConfiguration);
        JobProgress jobProgress = mock(JobProgress.class, Answers.RETURNS_SMART_NULLS);
        when(jobProgress.getIncrementalPosition(anyString())).thenReturn(Optional.of(new PlaceholderPosition()));
        mockJobContext.setInitProgress(jobProgress);
        new RuleAlteredJobPreparer().prepare(mockJobContext);
    }
    
    @Test
    public void assertCleanupSuccess() {
        JobConfiguration jobConfiguration = JobConfigurationBuilder.createJobConfiguration();
        new RuleAlteredJobPreparer().cleanup(jobConfiguration);
    }
}
