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

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.readwritesplitting.route.standard.filter.ReadDataSourcesFilter;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;

import java.util.List;

/**
 * Standard data source router for readwrite-splitting.
 */
public final class StandardReadwriteSplittingDataSourceRouter {
    
    /**
     * Route to data source.
     *
     * @param rule Readwrite-splitting data source rule
     * @return routed data source name
     */
    public String route(final ReadwriteSplittingDataSourceRule rule) {
        return rule.getLoadBalancer().getDataSource(rule.getName(), rule.getWriteDataSource(), getFilteredReadDataSources(rule));
    }
    
    private List<String> getFilteredReadDataSources(final ReadwriteSplittingDataSourceRule rule) {
        List<String> result = rule.getReadwriteSplittingGroup().getReadDataSources();
        for (ReadDataSourcesFilter each : ShardingSphereServiceLoader.getServiceInstances(ReadDataSourcesFilter.class)) {
            result = each.filter(rule, result);
        }
        return result;
    }
}
