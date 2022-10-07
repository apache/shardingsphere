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

package org.apache.shardingsphere.data.pipeline.api;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobAPIImpl;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPIImpl;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PipelineJobPublicAPIFactoryTest {
    
    @Test
    public void assertGetInventoryIncrementalJobPublicAPI() {
        Collection<Pair<JobType, Class<? extends InventoryIncrementalJobPublicAPI>>> paramResult = new LinkedList<>();
        paramResult.add(Pair.of(JobType.MIGRATION, MigrationJobAPIImpl.class));
        for (Pair<JobType, Class<? extends InventoryIncrementalJobPublicAPI>> each : paramResult) {
            assertThat(PipelineJobPublicAPIFactory.getInventoryIncrementalJobPublicAPI(each.getKey().getTypeName()), instanceOf(each.getValue()));
        }
    }
    
    @Test
    public void assertGetMigrationJobPublicAPI() {
        assertThat(PipelineJobPublicAPIFactory.getMigrationJobPublicAPI(), instanceOf(MigrationJobAPIImpl.class));
    }
    
    @Test
    public void assertGetConsistencyCheckJobPublicAPI() {
        assertThat(PipelineJobPublicAPIFactory.getConsistencyCheckJobPublicAPI(), instanceOf(ConsistencyCheckJobAPIImpl.class));
    }
}
