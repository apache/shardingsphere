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
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Readwrite-splitting data source mapper rule attribute.
 */
@RequiredArgsConstructor
public final class ReadwriteSplittingDataSourceMapperRuleAttribute implements DataSourceMapperRuleAttribute {
    
    private final Collection<ReadwriteSplittingDataSourceRule> dataSourceRules;
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>();
        for (ReadwriteSplittingDataSourceRule each : dataSourceRules) {
            result.put(each.getName(), each.getReadwriteSplittingGroup().getAllDataSources());
        }
        return result;
    }
}
