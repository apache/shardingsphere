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

package org.apache.shardingsphere.mask.rule.changed;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.metadata.nodepath.MaskRuleNodePathProvider;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mode.processor.AlgorithmChangedProcessor;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Mask algorithm changed processor.
 */
public final class MaskAlgorithmChangedProcessor extends AlgorithmChangedProcessor<MaskRuleConfiguration> {
    
    public MaskAlgorithmChangedProcessor() {
        super(MaskRule.class);
    }
    
    @Override
    protected MaskRuleConfiguration createEmptyRuleConfiguration() {
        return new MaskRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>());
    }
    
    @Override
    protected Map<String, AlgorithmConfiguration> getAlgorithmConfigurations(final MaskRuleConfiguration currentRuleConfig) {
        return currentRuleConfig.getMaskAlgorithms();
    }
    
    @Override
    public String getType() {
        return MaskRuleNodePathProvider.RULE_TYPE + "." + MaskRuleNodePathProvider.MASK_ALGORITHMS;
    }
}
