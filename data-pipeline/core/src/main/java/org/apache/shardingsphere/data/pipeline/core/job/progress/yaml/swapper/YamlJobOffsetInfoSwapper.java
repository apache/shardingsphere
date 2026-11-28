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

package org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper;

import org.apache.shardingsphere.data.pipeline.core.job.progress.JobOffsetInfo;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.config.YamlJobOffsetInfo;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * Yaml job offset info swapper.
 */
public final class YamlJobOffsetInfoSwapper implements YamlConfigurationSwapper<YamlJobOffsetInfo, JobOffsetInfo> {
    
    @Override
    public YamlJobOffsetInfo swapToYamlConfiguration(final JobOffsetInfo data) {
        YamlJobOffsetInfo result = new YamlJobOffsetInfo();
        result.setTargetSchemaTableCreated(data.isTargetSchemaTableCreated());
        return result;
    }
    
    @Override
    public JobOffsetInfo swapToObject(final YamlJobOffsetInfo yamlConfig) {
        return new JobOffsetInfo(yamlConfig.isTargetSchemaTableCreated());
    }
}
