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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.fixture;

import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.constant.ExportableItemConstants;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ReadwriteSplittingRuleExportableFixture implements ExportableRule {
    
    @Override
    public String getType() {
        return null;
    }
    
    @Override
    public Map<String, Supplier<Object>> getExportedMethods() {
        Map<String, Supplier<Object>> result = new HashMap<>(4, 1);
        result.put(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE, this::exportDynamicDataSources);
        result.put(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, this::exportStaticDataSources);
        return result;
    }
    
    private Map<String, Map<String, String>> exportDynamicDataSources() {
        Map<String, Map<String, String>> result = new HashMap<>(1, 1);
        result.put("readwrite_ds", getAutoAwareDataSources());
        return result;
    }
    
    private Map<String, String> getAutoAwareDataSources() {
        Map<String, String> result = new HashMap<>(3, 1);
        result.put(ExportableItemConstants.AUTO_AWARE_DATA_SOURCE_NAME, "ha_group");
        result.put(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME, "write_ds");
        result.put(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES, "read_ds_0,read_ds_1");
        return result;
    }
    
    private Map<String, Map<String, String>> exportStaticDataSources() {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        Map<String, String> staticRule = new LinkedHashMap<>(2, 1);
        staticRule.put(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME, "ds_0");
        staticRule.put(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES, "ds_1");
        result.put("static_rule_1", staticRule);
        return result;
    }
}
