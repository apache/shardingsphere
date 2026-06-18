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

package org.apache.shardingsphere.readwritesplitting.route.standard;

import org.apache.shardingsphere.readwritesplitting.route.standard.filter.ReadDataSourcesFilter;
import org.apache.shardingsphere.readwritesplitting.route.standard.filter.type.DisabledReadDataSourcesFilter;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Standard data source router for readwrite-splitting.
 */
public final class StandardReadwriteSplittingDataSourceRouter {
    
    private static final Collection<ReadDataSourcesFilter> FILTERS = Collections.singleton(new DisabledReadDataSourcesFilter());
    
    /**
     * Route to data source.
     *
     * @param rule Readwrite-splitting data source rule
     * @return routed data source name
     */
    public String route(final ReadwriteSplittingDataSourceGroupRule rule) {
        return rule.getLoadBalancer().getTargetName(rule.getName(), getFilteredReadDataSources(rule));
    }
    
    private List<String> getFilteredReadDataSources(final ReadwriteSplittingDataSourceGroupRule rule) {
        List<String> result = rule.getReadwriteSplittingGroup().getReadDataSources();
        for (ReadDataSourcesFilter each : FILTERS) {
            result = each.filter(rule, result);
        }
        return result;
    }
}
