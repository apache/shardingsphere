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

package org.apache.shardingsphere.readwritesplitting.rule.attribute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.rule.attribute.exportable.ExportableRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.infra.rule.attribute.exportable.constant.ExportableItemConstants;
import org.apache.shardingsphere.readwritesplitting.group.type.StaticReadwriteSplittingGroup;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Readwrite-splitting exportable rule attribute.
 */
@RequiredArgsConstructor
public final class ReadwriteSplittingExportableRuleAttribute implements ExportableRuleAttribute {
    
    private final Map<String, ReadwriteSplittingDataSourceRule> dataSourceRules;
    
    @Override
    public Map<String, Object> getExportData() {
        Map<String, Object> result = new HashMap<>(2, 1F);
        result.put(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, exportStaticDataSources());
        return result;
    }
    
    private Map<String, Map<String, String>> exportStaticDataSources() {
        Map<String, Map<String, String>> result = new LinkedHashMap<>(dataSourceRules.size(), 1F);
        for (ReadwriteSplittingDataSourceRule each : dataSourceRules.values()) {
            if (each.getReadwriteSplittingGroup() instanceof StaticReadwriteSplittingGroup) {
                Map<String, String> exportedDataSources = new LinkedHashMap<>(2, 1F);
                exportedDataSources.put(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME, each.getWriteDataSource());
                exportedDataSources.put(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES, String.join(",", each.getReadwriteSplittingGroup().getReadDataSources()));
                result.put(each.getName(), exportedDataSources);
            }
        }
        return result;
    }
}
