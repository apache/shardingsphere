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

package org.apache.shardingsphere.shadow.api.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.RuleConfiguration;

import java.util.Map;

/**
 * Shadow rule configuration.
 */
@Getter
public final class ShadowRuleConfiguration implements RuleConfiguration {
    
    private final String column;
    
    private final Map<String, String> shadowMappings;
    
    public ShadowRuleConfiguration(final String column, final Map<String, String> shadowMappings) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(column), "Column is required.");
        Preconditions.checkArgument(!shadowMappings.isEmpty(), "ShadowMappings is required.");
        this.column = column;
        this.shadowMappings = shadowMappings;
    }
}
