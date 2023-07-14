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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.yaml;

import com.google.common.base.Strings;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCheckIgnoredType;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.yaml.YamlDataConsistencyCheckResult.YamlDataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.yaml.YamlDataConsistencyCheckResult.YamlDataConsistencyCountCheckResult;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * Yaml data consistency check result swapper.
 */
public final class YamlDataConsistencyCheckResultSwapper implements YamlConfigurationSwapper<YamlDataConsistencyCheckResult, DataConsistencyCheckResult> {
    
    @Override
    public YamlDataConsistencyCheckResult swapToYamlConfiguration(final DataConsistencyCheckResult data) {
        YamlDataConsistencyCheckResult result = new YamlDataConsistencyCheckResult();
        if (data.isIgnored()) {
            result.setIgnoredType(data.getIgnoredType().name());
            return result;
        }
        YamlDataConsistencyCountCheckResult countCheckResult = new YamlDataConsistencyCountCheckResult();
        countCheckResult.setSourceRecordsCount(data.getCountCheckResult().getSourceRecordsCount());
        countCheckResult.setTargetRecordsCount(data.getCountCheckResult().getTargetRecordsCount());
        countCheckResult.setMatched(data.getContentCheckResult().isMatched());
        result.setCountCheckResult(countCheckResult);
        YamlDataConsistencyContentCheckResult contentCheckResult = new YamlDataConsistencyContentCheckResult();
        contentCheckResult.setMatched(data.getContentCheckResult().isMatched());
        result.setContentCheckResult(contentCheckResult);
        return result;
    }
    
    @Override
    public DataConsistencyCheckResult swapToObject(final YamlDataConsistencyCheckResult yamlConfig) {
        if (null == yamlConfig) {
            return null;
        }
        if (!Strings.isNullOrEmpty(yamlConfig.getIgnoredType())) {
            return new DataConsistencyCheckResult(DataConsistencyCheckIgnoredType.valueOf(yamlConfig.getIgnoredType()));
        }
        YamlDataConsistencyCountCheckResult yamlCountCheck = yamlConfig.getCountCheckResult();
        DataConsistencyCountCheckResult countCheckResult = new DataConsistencyCountCheckResult(yamlCountCheck.getSourceRecordsCount(), yamlCountCheck.getTargetRecordsCount());
        DataConsistencyContentCheckResult contentCheckResult = new DataConsistencyContentCheckResult(yamlConfig.getContentCheckResult().isMatched());
        return new DataConsistencyCheckResult(countCheckResult, contentCheckResult);
    }
    
    /**
     * Swap string to data consistency check result.
     *
     * @param param parameter
     * @return data consistency check result
     */
    public DataConsistencyCheckResult swapToObject(final String param) {
        return swapToObject(YamlEngine.unmarshal(param, YamlDataConsistencyCheckResult.class, true));
    }
}
