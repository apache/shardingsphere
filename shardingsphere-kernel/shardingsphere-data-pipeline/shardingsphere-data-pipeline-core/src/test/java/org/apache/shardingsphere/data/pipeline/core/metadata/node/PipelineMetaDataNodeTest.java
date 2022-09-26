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

package org.apache.shardingsphere.data.pipeline.core.metadata.node;

import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PipelineMetaDataNodeTest {
    
    private final String migrationMetaDataRootPath = "/pipeline/migration/metadata";
    
    private final String jobId = "j0101001";
    
    private final String jobsPath = "/pipeline/jobs";
    
    private final String jobRootPath = jobsPath + "/" + jobId;
    
    private final String jobCheckRootPath = jobRootPath + "/check";
    
    @Test
    public void assertGetMetaDataDataSourcesPath() {
        assertThat(PipelineMetaDataNode.getMetaDataDataSourcesPath(JobType.MIGRATION), is(migrationMetaDataRootPath + "/dataSources"));
    }
    
    @Test
    public void assertGetMetaDataProcessConfigPath() {
        assertThat(PipelineMetaDataNode.getMetaDataProcessConfigPath(JobType.MIGRATION), is(migrationMetaDataRootPath + "/processConfig"));
    }
    
    @Test
    public void assertGetElasticJobNamespace() {
        assertThat(PipelineMetaDataNode.getElasticJobNamespace(), is(jobsPath));
    }
    
    @Test
    public void assertGetJobRootPath() {
        assertThat(PipelineMetaDataNode.getJobRootPath(jobId), is(jobRootPath));
    }
    
    @Test
    public void assertGetJobOffsetPath() {
        assertThat(PipelineMetaDataNode.getJobOffsetPath(jobId), is(jobRootPath + "/offset"));
    }
    
    @Test
    public void assertGetJobOffsetItemPath() {
        assertThat(PipelineMetaDataNode.getJobOffsetItemPath(jobId, 0), is(jobRootPath + "/offset/0"));
    }
    
    @Test
    public void assertGetJobConfigPath() {
        assertThat(PipelineMetaDataNode.getJobConfigPath(jobId), is(jobRootPath + "/config"));
    }
    
    @Test
    public void assertGetCheckLatestJobIdPath() {
        assertThat(PipelineMetaDataNode.getCheckLatestJobIdPath(jobId), is(jobCheckRootPath + "/latest_job_id"));
    }
    
    @Test
    public void assertGetCheckJobResultPath() {
        assertThat(PipelineMetaDataNode.getCheckJobResultPath(jobId, "j02fx123"), is(jobCheckRootPath + "/job_ids/j02fx123"));
    }
    
    @Test
    public void assertGetCheckJobIdsPath() {
        assertThat(PipelineMetaDataNode.getCheckJobIdsRootPath(jobId), is(jobCheckRootPath + "/job_ids"));
    }
    
    @Test
    public void assertGetCheckJobIdPath() {
        String checkJobId = "j0201001";
        assertThat(PipelineMetaDataNode.getCheckJobIdPath(jobId, checkJobId), is(jobCheckRootPath + "/job_ids/" + checkJobId));
    }
    
    @Test
    public void assertGetJobBarrierEnablePath() {
        assertThat(PipelineMetaDataNode.getJobBarrierEnablePath(jobId), is(jobRootPath + "/barrier/enable"));
    }
    
    @Test
    public void assertGetJobBarrierDisablePath() {
        assertThat(PipelineMetaDataNode.getJobBarrierDisablePath(jobId), is(jobRootPath + "/barrier/disable"));
    }
}
