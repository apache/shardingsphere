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

package org.apache.shardingsphere.infra.metadata.database.resource;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Physical data source aggregator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PhysicalDataSourceAggregator {
    
    /**
     * Get aggregated data sources.
     *
     * @param dataSourceMap data source map
     * @param builtRules built rules
     * @return aggregated data sources
     */
    public static Map<String, DataSource> getAggregatedDataSources(final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        Map<String, DataSource> result = new CaseInsensitiveMap<>(dataSourceMap);
        for (ShardingSphereRule each : builtRules) {
            Optional<DataSourceMapperRuleAttribute> ruleAttribute = each.getAttributes().findAttribute(DataSourceMapperRuleAttribute.class);
            if (ruleAttribute.isPresent()) {
                result = getAggregatedDataSources(result, ruleAttribute.get());
            }
        }
        return result;
    }
    
    private static Map<String, DataSource> getAggregatedDataSources(final Map<String, DataSource> dataSourceMap, final DataSourceMapperRuleAttribute ruleAttribute) {
        Map<String, DataSource> result = new CaseInsensitiveMap<>();
        for (Entry<String, Collection<String>> entry : ruleAttribute.getDataSourceMapper().entrySet()) {
            for (String each : entry.getValue()) {
                if (dataSourceMap.containsKey(each)) {
                    result.putIfAbsent(entry.getKey(), dataSourceMap.remove(each));
                }
            }
        }
        result.putAll(dataSourceMap);
        return result;
    }
}
