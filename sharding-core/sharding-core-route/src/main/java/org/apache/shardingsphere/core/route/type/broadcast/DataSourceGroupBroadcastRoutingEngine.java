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

package org.apache.shardingsphere.core.route.type.broadcast;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.route.type.RoutingEngine;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

/**
 * Data source group broadcast routing engine.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class DataSourceGroupBroadcastRoutingEngine implements RoutingEngine {
    
    private final ShardingRule shardingRule;
    
    @Override
    public RoutingResult route() {
        RoutingResult result = new RoutingResult();
        Collection<Set<String>> broadcastDataSourceGroup = getBroadcastDataSourceGroup(getDataSourceGroup());
        for (Set<String> each : broadcastDataSourceGroup) {
            result.getRoutingUnits().add(new RoutingUnit(getRandomDataSourceName(each)));
        }
        return result;
    }
    
    private Collection<Set<String>> getBroadcastDataSourceGroup(final Collection<Set<String>> dataSourceGroup) {
        Collection<Set<String>> result = new LinkedList<>();
        for (Set<String> each : dataSourceGroup) {
            result = getCandidateDataSourceGroup(result, each);
        }
        return result;
    }
    
    private Collection<Set<String>> getDataSourceGroup() {
        Collection<Set<String>> result = new LinkedList<>();
        for (TableRule each : shardingRule.getTableRules()) {
            result.add(each.getDataNodeGroups().keySet());
        }
        if (null != shardingRule.getShardingDataSourceNames().getDefaultDataSourceName()) {
            result.add(Sets.newHashSet(shardingRule.getShardingDataSourceNames().getDefaultDataSourceName()));
        }
        return result;
    }
    
    private Collection<Set<String>> getCandidateDataSourceGroup(final Collection<Set<String>> dataSourceSetGroup, final Set<String> compareSet) {
        Collection<Set<String>> result = new LinkedList<>();
        Set<String> intersectionSet;
        if (dataSourceSetGroup.isEmpty()) {
            result.add(compareSet);
            return result;
        }
        boolean hasIntersection = false;
        for (Set<String> each : dataSourceSetGroup) {
            intersectionSet = Sets.intersection(each, compareSet);
            if (!intersectionSet.isEmpty()) {
                result.add(intersectionSet);
                hasIntersection = true;
            } else {
                result.add(each);
            }
        }
        if (!hasIntersection) {
            result.add(compareSet);
        }
        return result;
    }
    
    private String getRandomDataSourceName(final Collection<String> dataSourceNames) {
        return Lists.newArrayList(dataSourceNames).get(new Random().nextInt(dataSourceNames.size()));
    }
}
