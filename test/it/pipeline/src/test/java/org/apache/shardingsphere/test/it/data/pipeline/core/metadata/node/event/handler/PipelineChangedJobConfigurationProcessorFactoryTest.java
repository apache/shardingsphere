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

package org.apache.shardingsphere.test.it.data.pipeline.core.metadata.node.event.handler;

import org.apache.shardingsphere.data.pipeline.core.job.type.ConsistencyCheckJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.event.handler.PipelineChangedJobConfigurationProcessorFactory;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.event.handler.PipelineChangedJobConfigurationProcessor;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.metadata.processor.ConsistencyCheckChangedJobConfigurationProcessor;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.data.pipeline.scenario.migration.metadata.processor.MigrationChangedJobConfigurationProcessor;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PipelineChangedJobConfigurationProcessorFactoryTest {
    
    @Test
    public void assertGetInstance() {
        Optional<PipelineChangedJobConfigurationProcessor> migrationProcessor = PipelineChangedJobConfigurationProcessorFactory.findInstance(new MigrationJobType());
        assertTrue(migrationProcessor.isPresent());
        assertThat(migrationProcessor.get(), instanceOf(MigrationChangedJobConfigurationProcessor.class));
        Optional<PipelineChangedJobConfigurationProcessor> consistencyCheckProcessor = PipelineChangedJobConfigurationProcessorFactory.findInstance(new ConsistencyCheckJobType());
        assertTrue(consistencyCheckProcessor.isPresent());
        assertThat(consistencyCheckProcessor.get(), instanceOf(ConsistencyCheckChangedJobConfigurationProcessor.class));
    }
}
