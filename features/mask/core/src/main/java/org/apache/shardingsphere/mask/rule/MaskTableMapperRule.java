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

package org.apache.shardingsphere.mask.rule;

import org.apache.shardingsphere.infra.rule.identifier.type.table.TableMapperRule;
import org.apache.shardingsphere.infra.rule.identifier.type.table.TableNamesMapper;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;

import java.util.Collection;

/**
 * Mask table mapper rule.
 */
public final class MaskTableMapperRule implements TableMapperRule {
    
    private final TableNamesMapper logicalTableMapper;
    
    public MaskTableMapperRule(final Collection<MaskTableRuleConfiguration> tables) {
        logicalTableMapper = new TableNamesMapper();
        tables.stream().map(MaskTableRuleConfiguration::getName).forEach(logicalTableMapper::put);
    }
    
    @Override
    public TableNamesMapper getLogicTableMapper() {
        return logicalTableMapper;
    }
    
    @Override
    public TableNamesMapper getDistributedTableMapper() {
        return new TableNamesMapper();
    }
    
    @Override
    public TableNamesMapper getEnhancedTableMapper() {
        return new TableNamesMapper();
    }
}
