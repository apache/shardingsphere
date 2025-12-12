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

package org.apache.shardingsphere.data.pipeline.core.job.id;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobId;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PipelineJobIdUtilsTest {
    
    @Test
    void assertParseJobTypeWithInvalidJobId() {
        assertThrows(IllegalArgumentException.class, () -> PipelineJobIdUtils.parseJobType("123"));
        assertThrows(IllegalArgumentException.class, () -> PipelineJobIdUtils.parseJobType("12345678901234567890"));
    }
    
    @Test
    void assertParseJobType() {
        PipelineContextKey contextKey = new PipelineContextKey("foo_db", InstanceType.JDBC);
        String jobId = PipelineJobIdUtils.marshal(new MigrationJobId(contextKey, Collections.singletonList("foo_tbl:foo_ds.foo_tbl_0,foo_ds.foo_tbl_1")));
        assertThat(PipelineJobIdUtils.parseJobType(jobId), isA(MigrationJobType.class));
    }
    
    @Test
    void assertParseContextKeyWithJDBCInstanceType() {
        PipelineContextKey contextKey = new PipelineContextKey("foo_db", InstanceType.JDBC);
        String jobId = PipelineJobIdUtils.marshal(new MigrationJobId(contextKey, Collections.singletonList("foo_tbl:foo_ds.foo_tbl_0,foo_ds.foo_tbl_1")));
        assertThat(PipelineJobIdUtils.parseContextKey(jobId).getDatabaseName(), is("foo_db"));
    }
    
    @Test
    void assertParseContextKeyWithProxyInstanceType() {
        PipelineContextKey contextKey = new PipelineContextKey("foo_db", InstanceType.PROXY);
        String jobId = PipelineJobIdUtils.marshal(new MigrationJobId(contextKey, Collections.singletonList("foo_tbl:foo_ds.foo_tbl_0,foo_ds.foo_tbl_1")));
        assertThat(PipelineJobIdUtils.parseContextKey(jobId).getDatabaseName(), is(""));
    }
}
