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

package org.apache.shardingsphere.data.pipeline.core.datasource.creator;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.creator.impl.StandardPipelineDataSourceCreator;
import org.apache.shardingsphere.driver.data.pipeline.datasource.creator.ShardingSpherePipelineDataSourceCreator;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public final class PipelineDataSourceCreatorFactoryTest {
    
    @Test
    public void assertGetInstance() {
        Collection<Pair<String, Class<? extends PipelineDataSourceCreator>>> paramResult = Arrays.asList(
                Pair.of(StandardPipelineDataSourceConfiguration.TYPE, StandardPipelineDataSourceCreator.class),
                Pair.of(ShardingSpherePipelineDataSourceConfiguration.TYPE, ShardingSpherePipelineDataSourceCreator.class));
        for (Pair<String, Class<? extends PipelineDataSourceCreator>> each : paramResult) {
            PipelineDataSourceCreator actual = PipelineDataSourceCreatorFactory.getInstance(each.getKey());
            assertThat(actual, instanceOf(each.getValue()));
        }
    }
}
