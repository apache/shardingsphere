/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.rule;

import io.shardingsphere.api.config.TableRuleConfiguration;
import io.shardingsphere.core.keygen.KeyGenerator;
import lombok.Getter;

import java.util.Collection;

/**
 * Broadcast table rule configuration.
 *
 * @author maxiaoguang
 */
@Getter
public final class BroadcastTableRule {
    
    private final String logicTable;
    
    private final String generateKeyColumn;
    
    private final KeyGenerator keyGenerator;
    
    public BroadcastTableRule(final TableRuleConfiguration tableRuleConfig, final Collection<String> dataSourceNames) {
        logicTable = tableRuleConfig.getLogicTable().toLowerCase();
        generateKeyColumn = tableRuleConfig.getKeyGeneratorColumnName();
        keyGenerator = tableRuleConfig.getKeyGenerator();
    }

}
