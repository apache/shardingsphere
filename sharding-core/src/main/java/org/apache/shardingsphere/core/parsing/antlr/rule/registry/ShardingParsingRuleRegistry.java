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

package org.apache.shardingsphere.core.parsing.antlr.rule.registry;

import java.util.List;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parsing.antlr.rule.jaxb.loader.RuleDefinitionFileConstant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding parsing rule registry.
 *
 * @author duhongjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShardingParsingRuleRegistry extends ParsingRuleRegistry {
    
    private static volatile ParsingRuleRegistry instance;
    
    /**
     * Get singleton instance of parsing rule registry.
     *
     * @return instance of parsing rule registry
     */
    public static ParsingRuleRegistry getInstance() {
        if (null == instance) {
            synchronized (ShardingParsingRuleRegistry.class) {
                if (null == instance) {
                    instance = new ShardingParsingRuleRegistry();
                    instance.init();
                }
            }
        }
        return instance;
    }
    
    @Override
    protected void fillSelfCommonFilePath(final DatabaseType databaseType, final List<String> fillerFilePaths, final List<String> extractorFilePaths, final List<String> sqlStateRuleFilePaths) {
        fillerFilePaths.add(RuleDefinitionFileConstant.getShardingCommonFillerRuleDefinitionFileName());
        if (DatabaseType.MySQL == databaseType) {
            fillerFilePaths.add(RuleDefinitionFileConstant.getFillerRuleDefinitionFileName(RuleDefinitionFileConstant.SHARDING_ROOT_PATH, databaseType));
        }
    }
    
    @Override
    protected String getExtractorRuleDefinitionFileName(final DatabaseType databaseType) {
        return RuleDefinitionFileConstant.getExtractorRuleDefinitionFileName(RuleDefinitionFileConstant.SHARDING_ROOT_PATH, databaseType);
    }
    
    @Override
    protected String getStatementRuleDefinitionFileName(final DatabaseType databaseType) {
        return RuleDefinitionFileConstant.getSQLStatementRuleDefinitionFileName(RuleDefinitionFileConstant.SHARDING_ROOT_PATH, databaseType);
    }
}

