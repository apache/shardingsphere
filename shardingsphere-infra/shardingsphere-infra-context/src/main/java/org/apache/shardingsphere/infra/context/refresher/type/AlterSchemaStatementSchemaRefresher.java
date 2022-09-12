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
import org.apache.shardingsphere.infra.context.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.event.AlterSchemaEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.MetaDataRefreshedEvent;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.AlterSchemaStatementHandler;

import java.util.Collection;
import java.util.Optional;

/**
 * Schema refresher for alter schema statement.
 */
public final class AlterSchemaStatementSchemaRefresher implements MetaDataRefresher<AlterSchemaStatement> {
    
    @Override
    public Optional<MetaDataRefreshedEvent> refresh(final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                                                    final String schemaName, final AlterSchemaStatement sqlStatement, final ConfigurationProperties props) {
        Optional<String> renameSchemaName = AlterSchemaStatementHandler.getRenameSchema(sqlStatement).map(optional -> optional.getValue().toLowerCase());
        if (!renameSchemaName.isPresent()) {
            return Optional.empty();
        }
        String actualSchemaName = sqlStatement.getSchemaName().getValue().toLowerCase();
        putSchemaMetaData(database, actualSchemaName, renameSchemaName.get(), logicDataSourceNames);
        removeSchemaMetaData(database, actualSchemaName);
        AlterSchemaEvent event = new AlterSchemaEvent(database.getName(), actualSchemaName, renameSchemaName.get(), database.getSchema(renameSchemaName.get()));
        return Optional.of(event);
    }
    
    private void removeSchemaMetaData(final ShardingSphereDatabase database, final String schemaName) {
        ShardingSphereSchema schema = new ShardingSphereSchema(database.getSchema(schemaName).getTables(), database.getSchema(schemaName).getViews());
        database.removeSchema(schemaName);
        Collection<MutableDataNodeRule> rules = database.getRuleMetaData().findRules(MutableDataNodeRule.class);
        for (String each : schema.getAllTableNames()) {
            removeDataNode(rules, schemaName, each);
        }
    }
    
    private void removeDataNode(final Collection<MutableDataNodeRule> rules, final String schemaName, final String tableName) {
        for (MutableDataNodeRule each : rules) {
            each.remove(schemaName, tableName);
        }
    }
    
    private void putSchemaMetaData(final ShardingSphereDatabase database, final String schemaName, final String renameSchemaName, final Collection<String> logicDataSourceNames) {
        ShardingSphereSchema schema = database.getSchema(schemaName);
        database.putSchema(renameSchemaName, schema);
        Collection<MutableDataNodeRule> rules = database.getRuleMetaData().findRules(MutableDataNodeRule.class);
        for (String each : schema.getAllTableNames()) {
            if (containsInImmutableDataNodeContainedRule(each, database)) {
                continue;
            }
            putDataNode(rules, logicDataSourceNames.iterator().next(), renameSchemaName, each);
        }
    }
    
    private void putDataNode(final Collection<MutableDataNodeRule> rules, final String dataSourceName, final String schemaName, final String tableName) {
        for (MutableDataNodeRule each : rules) {
            each.put(dataSourceName, schemaName, tableName);
        }
    }
    
    private boolean containsInImmutableDataNodeContainedRule(final String tableName, final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findRules(DataNodeContainedRule.class).stream()
                .filter(each -> !(each instanceof MutableDataNodeRule)).anyMatch(each -> each.getAllTables().contains(tableName));
    }
    
    @Override
    public String getType() {
        return AlterSchemaStatement.class.getName();
    }
}
