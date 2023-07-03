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

import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.common.job.type.JobType;
import org.apache.shardingsphere.data.pipeline.common.job.type.JobTypeFactory;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

class JobTypeFactoryTest {
    
    @Test
    void assertGetInstance() {
        Collection<Pair<String, Class<? extends JobType>>> paramResult = Arrays.asList(
                Pair.of(MigrationJobType.TYPE_CODE, MigrationJobType.class), Pair.of(ConsistencyCheckJobType.TYPE_CODE, ConsistencyCheckJobType.class));
        for (Pair<String, Class<? extends JobType>> each : paramResult) {
            JobType actual = JobTypeFactory.getInstance(each.getKey());
            assertThat(actual, instanceOf(each.getValue()));
        }
    }
}
