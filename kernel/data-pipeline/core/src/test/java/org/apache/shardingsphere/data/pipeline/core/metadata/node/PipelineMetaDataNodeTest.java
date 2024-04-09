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

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PipelineMetaDataNodeTest {
    
    private final String migrationMetaDataRootPath = "/pipeline/fixture/metadata";
    
    private final String jobId = "j0101001";
    
    private final String jobsPath = "/pipeline/jobs";
    
    private final String jobRootPath = jobsPath + "/" + jobId;
    
    private final String jobCheckRootPath = jobRootPath + "/check";
    
    @Test
    void assertGetMetaDataDataSourcesPath() {
        MatcherAssert.assertThat(PipelineMetaDataNode.getMetaDataDataSourcesPath("FIXTURE"), is(migrationMetaDataRootPath + "/data_sources"));
    }
    
    @Test
    void assertGetMetaDataProcessConfigPath() {
        assertThat(PipelineMetaDataNode.getMetaDataProcessConfigPath("FIXTURE"), is(migrationMetaDataRootPath + "/process_config"));
    }
    
    @Test
    void assertGetElasticJobNamespace() {
        assertThat(PipelineMetaDataNode.getElasticJobNamespace(), is(jobsPath));
    }
    
    @Test
    void assertGetJobRootPath() {
        assertThat(PipelineMetaDataNode.getJobRootPath(jobId), is(jobRootPath));
    }
    
    @Test
    void assertGetJobOffsetPath() {
        assertThat(PipelineMetaDataNode.getJobOffsetPath(jobId), is(jobRootPath + "/offset"));
    }
    
    @Test
    void assertGetJobOffsetItemPath() {
        assertThat(PipelineMetaDataNode.getJobOffsetItemPath(jobId, 0), is(jobRootPath + "/offset/0"));
    }
    
    @Test
    void assertGetJobConfigPath() {
        assertThat(PipelineMetaDataNode.getJobConfigurationPath(jobId), is(jobRootPath + "/config"));
    }
    
    @Test
    void assertGetLatestCheckJobIdPath() {
        assertThat(PipelineMetaDataNode.getLatestCheckJobIdPath(jobId), is(jobCheckRootPath + "/latest_job_id"));
    }
    
    @Test
    void assertGetCheckJobResultPath() {
        assertThat(PipelineMetaDataNode.getCheckJobResultPath(jobId, "j02fx123"), is(jobCheckRootPath + "/job_ids/j02fx123"));
    }
    
    @Test
    void assertGetCheckJobIdsPath() {
        assertThat(PipelineMetaDataNode.getCheckJobIdsRootPath(jobId), is(jobCheckRootPath + "/job_ids"));
    }
    
    @Test
    void assertGetJobBarrierEnablePath() {
        assertThat(PipelineMetaDataNode.getJobBarrierEnablePath(jobId), is(jobRootPath + "/barrier/enable"));
    }
    
    @Test
    void assertGetJobBarrierDisablePath() {
        assertThat(PipelineMetaDataNode.getJobBarrierDisablePath(jobId), is(jobRootPath + "/barrier/disable"));
    }
}
