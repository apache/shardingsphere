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

package org.apache.shardingsphere.mask.event;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.metadata.nodepath.RuleNodePath;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mask.event.algorithm.AlterMaskAlgorithmEvent;
import org.apache.shardingsphere.mask.event.algorithm.DeleteMaskAlgorithmEvent;
import org.apache.shardingsphere.mask.event.table.AddMaskTableEvent;
import org.apache.shardingsphere.mask.event.table.AlterMaskTableEvent;
import org.apache.shardingsphere.mask.event.table.DeleteMaskTableEvent;
import org.apache.shardingsphere.mask.metadata.nodepath.MaskNodePath;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;

import java.util.Optional;

/**
 * Mask rule configuration event builder.
 */
public final class MaskRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    private final RuleNodePath maskRuleNodePath = MaskNodePath.getInstance();
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!maskRuleNodePath.getRoot().isValidatedPath(event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<String> tableName = maskRuleNodePath.getNamedItem(MaskNodePath.TABLES).getNameByActiveVersion(event.getKey());
        if (tableName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createMaskConfigEvent(databaseName, tableName.get(), event);
        }
        Optional<String> algorithmName = maskRuleNodePath.getNamedItem(MaskNodePath.ALGORITHMS).getNameByActiveVersion(event.getKey());
        if (algorithmName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createMaskAlgorithmEvent(databaseName, algorithmName.get(), event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createMaskConfigEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddMaskTableEvent(databaseName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterMaskTableEvent(databaseName, tableName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteMaskTableEvent(databaseName, tableName));
    }
    
    private Optional<GovernanceEvent> createMaskAlgorithmEvent(final String databaseName, final String algorithmName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterMaskAlgorithmEvent(databaseName, algorithmName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteMaskAlgorithmEvent(databaseName, algorithmName));
    }
}
