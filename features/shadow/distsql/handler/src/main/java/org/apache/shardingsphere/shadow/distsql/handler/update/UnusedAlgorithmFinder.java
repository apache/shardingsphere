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

package org.apache.shardingsphere.shadow.distsql.handler.update;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Unused algorithm finder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnusedAlgorithmFinder {
    
    /**
     * Find unused shadow algorithms.
     *
     * @param ruleConfig shadow rule configuration
     * @return found unused shadow algorithms
     */
    public static Collection<String> findUnusedShadowAlgorithm(final ShadowRuleConfiguration ruleConfig) {
        Collection<String> inUsedAlgorithms = ruleConfig.getTables().entrySet().stream().flatMap(entry -> entry.getValue().getShadowAlgorithmNames().stream()).collect(Collectors.toSet());
        if (null != ruleConfig.getDefaultShadowAlgorithmName()) {
            inUsedAlgorithms.add(ruleConfig.getDefaultShadowAlgorithmName());
        }
        return ruleConfig.getShadowAlgorithms().keySet().stream().filter(each -> !inUsedAlgorithms.contains(each)).collect(Collectors.toSet());
    }
}
