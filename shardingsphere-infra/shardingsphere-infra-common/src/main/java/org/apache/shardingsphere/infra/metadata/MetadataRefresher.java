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

package org.apache.shardingsphere.infra.metadata;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Metadata refresher.
 *
 */
public interface MetadataRefresher {
    
    /**
     * Find ShardingSphereRule collection by class.
     *
     * @param rules ShardingSphereRule collection
     * @param clazz target class
     * @param <R> type of target class
     * @return ShardingSphereRule collection of target class
     */
    default <R extends ShardingSphereRule> Collection<R> findShardingSphereRulesByClass(final Collection<ShardingSphereRule> rules, final Class<R> clazz) {
        return rules.stream().filter(each -> clazz.isAssignableFrom(each.getClass())).map(clazz::cast).collect(Collectors.toList());
    }
}
