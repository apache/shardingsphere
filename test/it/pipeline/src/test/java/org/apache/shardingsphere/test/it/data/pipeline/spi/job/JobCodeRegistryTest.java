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

package org.apache.shardingsphere.test.it.data.pipeline.spi.job;

import org.apache.shardingsphere.data.pipeline.common.job.type.JobCodeRegistry;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class JobCodeRegistryTest {
    
    @Test
    void assertGetJobType() {
        assertThat(JobCodeRegistry.getJobType(MigrationJobType.TYPE_CODE), is("MIGRATION"));
        assertThat(JobCodeRegistry.getJobType(ConsistencyCheckJobType.TYPE_CODE), is("CONSISTENCY_CHECK"));
    }
}
