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

import java.util.List;

/**
 * Shadow rule configuration.
 */
@Getter
public final class ShadowRuleConfiguration implements RuleConfiguration {
    
    private final String column;
    
    private final List<String> sourceDataSourceNames;
    
    private final List<String> shadowDataSourceNames;
    
    public ShadowRuleConfiguration(final String column, final List<String> sourceDataSourceNames, final List<String> shadowDataSourceNames) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(column), "Column is required.");
        Preconditions.checkArgument(!sourceDataSourceNames.isEmpty(), "SourceDataSourceNames is required.");
        Preconditions.checkArgument(!shadowDataSourceNames.isEmpty(), "ShadowDataSourceNames is required.");
        Preconditions.checkArgument(sourceDataSourceNames.size() == shadowDataSourceNames.size(), "SourceDataSourceNames and ShadowDataSourceNames size must same.");
        this.column = column;
        this.sourceDataSourceNames = sourceDataSourceNames;
        this.shadowDataSourceNames = shadowDataSourceNames;
    }
}
