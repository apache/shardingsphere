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

import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.api.job.JobSubType;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.RuleAlteredJobId;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class RuleAlteredJobIdTest {
    
    @Test
    public void assertSerialization() {
        RuleAlteredJobId jobId = new RuleAlteredJobId();
        jobId.setType(JobType.RULE_ALTERED.getValue());
        jobId.setFormatVersion(RuleAlteredJobId.CURRENT_VERSION);
        Pair<List<String>, List<String>> subTypesPair = getSubTypesPair();
        jobId.setSubTypes(subTypesPair.getLeft());
        jobId.setCurrentMetadataVersion(0);
        jobId.setNewMetadataVersion(1);
        jobId.setDatabaseName("sharding_db");
        String hexText = jobId.marshal();
        RuleAlteredJobId actual = RuleAlteredJobId.unmarshal(hexText);
        assertThat(actual.getFormatVersion(), is(jobId.getFormatVersion()));
        assertThat(actual.getSubTypes(), is(subTypesPair.getRight()));
        assertThat(actual.getCurrentMetadataVersion(), is(jobId.getCurrentMetadataVersion()));
        assertThat(actual.getNewMetadataVersion(), is(jobId.getNewMetadataVersion()));
        assertThat(actual.getDatabaseName(), is(jobId.getDatabaseName()));
    }
    
    private Pair<List<String>, List<String>> getSubTypesPair() {
        return Pair.of(Arrays.asList(JobSubType.ENCRYPTION.getValue(), JobSubType.SCALING.getValue()),
                Arrays.asList(JobSubType.SCALING.getValue(), JobSubType.ENCRYPTION.getValue()));
    }
}
