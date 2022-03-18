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

import org.apache.shardingsphere.data.pipeline.api.job.JobId;
import org.apache.shardingsphere.data.pipeline.api.job.JobIdCodec;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class JobIdCodecTest {
    
    @Test
    public void assertSerialization() {
        List<Integer> subTypes = Arrays.asList(2, 1);
        JobId jobId = new JobId(JobId.CURRENT_VERSION, subTypes, 0, 1, "sharding_db");
        String hexText = JobIdCodec.marshal(jobId);
        JobId actual = JobIdCodec.unmarshal(hexText);
        assertThat(actual.getFormatVersion(), is(jobId.getFormatVersion()));
        assertThat(actual.getSubTypes(), is(Arrays.asList(1, 2)));
        assertThat(actual.getCurrentMetadataVersion(), is(jobId.getCurrentMetadataVersion()));
        assertThat(actual.getNewMetadataVersion(), is(jobId.getNewMetadataVersion()));
        assertThat(actual.getSchemaName(), is(jobId.getSchemaName()));
    }
}
