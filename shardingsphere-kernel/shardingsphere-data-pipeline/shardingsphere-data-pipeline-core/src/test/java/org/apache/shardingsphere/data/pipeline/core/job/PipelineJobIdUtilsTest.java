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

package org.apache.shardingsphere.data.pipeline.core.job;

import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.api.job.RuleAlteredJobId;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class PipelineJobIdUtilsTest {
    
    @Test
    public void assertCodec() {
        RuleAlteredJobId pipelineJobId = new RuleAlteredJobId();
        pipelineJobId.setTypeCode(JobType.MIGRATION.getTypeCode());
        pipelineJobId.setFormatVersion(RuleAlteredJobId.CURRENT_VERSION);
        pipelineJobId.setDatabaseName("sharding_db");
        pipelineJobId.setCurrentMetadataVersion(0);
        pipelineJobId.setNewMetadataVersion(1);
        String jobId = PipelineJobIdUtils.marshalJobIdCommonPrefix(pipelineJobId) + "abcd";
        JobType actualJobType = PipelineJobIdUtils.parseJobType(jobId);
        assertThat(actualJobType, is(JobType.MIGRATION));
    }
}
