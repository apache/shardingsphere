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

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobId;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PipelineJobIdUtilsTest {
    
    @Test
    void assertParse() {
        for (InstanceType each : InstanceType.values()) {
            assertParse0(each);
        }
    }
    
    private void assertParse0(final InstanceType instanceType) {
        PipelineContextKey contextKey = new PipelineContextKey("sharding_db", instanceType);
        String jobId = PipelineJobIdUtils.marshal(new MigrationJobId(contextKey, Collections.singletonList("t_order:ds_0.t_order_0,ds_0.t_order_1")));
        assertThat(PipelineJobIdUtils.parseJobType(jobId), instanceOf(MigrationJobType.class));
        PipelineContextKey actualContextKey = PipelineJobIdUtils.parseContextKey(jobId);
        assertThat(actualContextKey.getInstanceType(), is(instanceType));
        assertThat(actualContextKey.getDatabaseName(), is(instanceType == InstanceType.PROXY ? "" : "sharding_db"));
    }
}
