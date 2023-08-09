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
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckIgnoredType;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.yaml.YamlTableDataConsistencyCheckResult.YamlTableDataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.yaml.YamlTableDataConsistencyCheckResult.YamlTableDataConsistencyCountCheckResult;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;

/**
 * Yaml table data consistency check result swapper.
 */
public final class YamlTableDataConsistencyCheckResultSwapper implements YamlConfigurationSwapper<YamlTableDataConsistencyCheckResult, TableDataConsistencyCheckResult> {
    
    @Override
    public YamlTableDataConsistencyCheckResult swapToYamlConfiguration(final TableDataConsistencyCheckResult data) {
        YamlTableDataConsistencyCheckResult result = new YamlTableDataConsistencyCheckResult();
        if (data.isIgnored()) {
            result.setIgnoredType(data.getIgnoredType().name());
            return result;
        }
        YamlTableDataConsistencyCountCheckResult countCheckResult = new YamlTableDataConsistencyCountCheckResult();
        countCheckResult.setSourceRecordsCount(data.getCountCheckResult().getSourceRecordsCount());
        countCheckResult.setTargetRecordsCount(data.getCountCheckResult().getTargetRecordsCount());
        countCheckResult.setMatched(data.getContentCheckResult().isMatched());
        result.setCountCheckResult(countCheckResult);
        YamlTableDataConsistencyContentCheckResult contentCheckResult = new YamlTableDataConsistencyContentCheckResult();
        contentCheckResult.setMatched(data.getContentCheckResult().isMatched());
        result.setContentCheckResult(contentCheckResult);
        return result;
    }
    
    @Override
    public TableDataConsistencyCheckResult swapToObject(final YamlTableDataConsistencyCheckResult yamlConfig) {
        if (null == yamlConfig) {
            return null;
        }
        if (!Strings.isNullOrEmpty(yamlConfig.getIgnoredType())) {
            return new TableDataConsistencyCheckResult(TableDataConsistencyCheckIgnoredType.valueOf(yamlConfig.getIgnoredType()));
        }
        YamlTableDataConsistencyCountCheckResult yamlCountCheck = yamlConfig.getCountCheckResult();
        TableDataConsistencyCountCheckResult countCheckResult = new TableDataConsistencyCountCheckResult(yamlCountCheck.getSourceRecordsCount(), yamlCountCheck.getTargetRecordsCount());
        TableDataConsistencyContentCheckResult contentCheckResult = new TableDataConsistencyContentCheckResult(yamlConfig.getContentCheckResult().isMatched());
        return new TableDataConsistencyCheckResult(countCheckResult, contentCheckResult);
    }
    
    /**
     * Swap string to data consistency check result.
     *
     * @param param parameter
     * @return data consistency check result
     */
    public TableDataConsistencyCheckResult swapToObject(final String param) {
        return swapToObject(YamlEngine.unmarshal(param, YamlTableDataConsistencyCheckResult.class, true));
    }
}
