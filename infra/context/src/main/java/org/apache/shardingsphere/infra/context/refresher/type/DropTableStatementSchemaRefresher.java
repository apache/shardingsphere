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

package org.apache.shardingsphere.infra.context.refresher.type;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.function.MutableRuleConfiguration;
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.event.MetaDataRefreshedEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;

import java.util.Collection;
import java.util.Optional;

/**
 * Schema refresher for drop table statement.
 */
public final class DropTableStatementSchemaRefresher implements MetaDataRefresher<DropTableStatement> {
    
    @Override
    public Optional<MetaDataRefreshedEvent> refresh(final ModeContextManager modeContextManager, final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                                                    final String schemaName, final DropTableStatement sqlStatement, final ConfigurationProperties props) {
        SchemaAlteredEvent event = new SchemaAlteredEvent(database.getName(), schemaName);
        sqlStatement.getTables().forEach(each -> {
            ShardingSphereSchema schema = database.getSchema(schemaName);
            if (null != schema) {
                schema.removeTable(each.getTableName().getIdentifier().getValue());
            }
            event.getDroppedTables().add(each.getTableName().getIdentifier().getValue());
        });
        Collection<MutableDataNodeRule> rules = database.getRuleMetaData().findRules(MutableDataNodeRule.class);
        Collection<MutableRuleConfiguration> ruleConfigurations = database.getRuleMetaData().findRuleConfigurations(MutableRuleConfiguration.class);
        for (SimpleTableSegment each : sqlStatement.getTables()) {
            removeDataNode(rules, schemaName, each);
            removeRuleConfiguration(ruleConfigurations, logicDataSourceNames.iterator().next(), schemaName, each);
        }
        modeContextManager.alterRuleConfiguration(database.getName(), database.getRuleMetaData().getConfigurations());
        return Optional.of(event);
    }
    
    private void removeDataNode(final Collection<MutableDataNodeRule> rules, final String schemaName, final SimpleTableSegment tobeRemovedSegment) {
        for (MutableDataNodeRule each : rules) {
            each.remove(schemaName, tobeRemovedSegment.getTableName().getIdentifier().getValue());
        }
    }
    
    private void removeRuleConfiguration(final Collection<MutableRuleConfiguration> ruleConfigs, final String dataSourceName, final String schemaName, final SimpleTableSegment tobeRemovedSegment) {
        for (MutableRuleConfiguration each : ruleConfigs) {
            each.remove(dataSourceName, schemaName, tobeRemovedSegment.getTableName().getIdentifier().getValue());
        }
    }
    
    @Override
    public String getType() {
        return DropTableStatement.class.getName();
    }
}
