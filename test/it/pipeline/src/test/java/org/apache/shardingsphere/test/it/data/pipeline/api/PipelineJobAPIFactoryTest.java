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

package org.apache.shardingsphere.test.it.data.pipeline.api;

import org.apache.shardingsphere.data.pipeline.core.api.PipelineJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.type.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.api.impl.ConsistencyCheckJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PipelineJobAPIFactoryTest {
    
    @Test
    public void assertGetMigrationJobAPI() {
        assertThat(PipelineJobAPIFactory.getPipelineJobAPI(new MigrationJobType()), instanceOf(MigrationJobAPI.class));
        assertThat(PipelineJobAPIFactory.getPipelineJobAPI(new MigrationJobType().getTypeName()), instanceOf(MigrationJobAPI.class));
    }
    
    @Test
    public void assertGetConsistencyCheckJobAPI() {
        assertThat(PipelineJobAPIFactory.getPipelineJobAPI(new ConsistencyCheckJobType()), instanceOf(ConsistencyCheckJobAPI.class));
        assertThat(PipelineJobAPIFactory.getPipelineJobAPI(new ConsistencyCheckJobType().getTypeName()), instanceOf(ConsistencyCheckJobAPI.class));
    }
}
