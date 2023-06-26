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

package org.apache.shardingsphere.infra.metadata.converter;

import lombok.Getter;

import java.util.regex.Pattern;

/**
 * Rule root node converter.
 */
public final class RuleRootNodeConverter {
    
    private static final String RULE_NODE_PREFIX = "/([\\w\\-]+)/([\\w\\-]+)/rules/";
    
    @Getter
    private final String ruleNodePrefix;
    
    private final Pattern rulePathPattern;
    
    public RuleRootNodeConverter(final String ruleType) {
        ruleNodePrefix = RULE_NODE_PREFIX + ruleType;
        rulePathPattern = Pattern.compile(ruleNodePrefix + "/.*", Pattern.CASE_INSENSITIVE);
    }
    
    /**
     * Is rule path.
     *
     * @param rulePath rule path to be judged
     * @return true or false
     */
    public boolean isRulePath(final String rulePath) {
        return rulePathPattern.matcher(rulePath).find();
    }
}
