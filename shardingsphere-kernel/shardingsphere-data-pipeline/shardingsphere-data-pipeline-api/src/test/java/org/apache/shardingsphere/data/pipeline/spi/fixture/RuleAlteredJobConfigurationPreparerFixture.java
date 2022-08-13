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

package org.apache.shardingsphere.data.pipeline.spi.fixture;

import org.apache.shardingsphere.data.pipeline.api.config.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobConfigurationPreparer;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineProcessConfiguration;

public final class RuleAlteredJobConfigurationPreparerFixture implements RuleAlteredJobConfigurationPreparer {
    
    @Override
    public void extendJobConfiguration(final YamlMigrationJobConfiguration yamlJobConfig) {
    }
    
    @Override
    public TaskConfiguration createTaskConfiguration(final MigrationJobConfiguration jobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        return null;
    }
}
