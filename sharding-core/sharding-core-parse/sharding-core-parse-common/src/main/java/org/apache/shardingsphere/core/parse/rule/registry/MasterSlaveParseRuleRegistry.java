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

package org.apache.shardingsphere.core.parse.rule.registry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parse.rule.jaxb.loader.RuleDefinitionFileConstant;

import java.util.Collection;
import java.util.Collections;

/**
 * Parse rule registry for master-slave.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MasterSlaveParseRuleRegistry extends ParseRuleRegistry {
    
    private static volatile ParseRuleRegistry instance;
    
    /**
     * Get singleton instance of parsing rule registry.
     *
     * @return instance of parsing rule registry
     */
    public static ParseRuleRegistry getInstance() {
        if (null == instance) {
            synchronized (MasterSlaveParseRuleRegistry.class) {
                if (null == instance) {
                    instance = new MasterSlaveParseRuleRegistry();
                }
            }
        }
        return instance;
    }
    
    @Override
    protected String getExtractorFile(final DatabaseType databaseType) {
        return RuleDefinitionFileConstant.getExtractorRuleDefinitionFileName(RuleDefinitionFileConstant.MASTER_SALVE_ROOT_PATH, databaseType);
    }
    
    @Override
    protected Collection<String> getFillerFiles(final DatabaseType databaseType) {
        return Collections.singletonList(RuleDefinitionFileConstant.getFillerRuleDefinitionFileName(RuleDefinitionFileConstant.MASTER_SALVE_ROOT_PATH, databaseType));
    }
    
    @Override
    protected String getStatementRuleFile(final DatabaseType databaseType) {
        return RuleDefinitionFileConstant.getSQLStatementRuleDefinitionFileName(RuleDefinitionFileConstant.MASTER_SALVE_ROOT_PATH, databaseType);
    }
}

