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
import org.apache.shardingsphere.infra.metadata.database.schema.event.DropSchemaEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.MetaDataRefreshedEvent;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropSchemaStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Schema refresher for drop schema statement.
 */
public final class DropSchemaStatementSchemaRefresher implements MetaDataRefresher<DropSchemaStatement> {
    
    @Override
    public Optional<MetaDataRefreshedEvent> refresh(final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                                                    final String schemaName, final DropSchemaStatement sqlStatement, final ConfigurationProperties props) {
        Collection<String> tobeRemovedTables = new LinkedHashSet<>();
        Collection<String> tobeRemovedSchemas = new LinkedHashSet<>();
        Collection<String> schemaNames = getSchemaNames(sqlStatement);
        for (String each : schemaNames) {
            ShardingSphereSchema schema = new ShardingSphereSchema(database.getSchema(schemaName).getTables(), database.getSchema(schemaName).getViews());
            database.removeSchema(schemaName);
            Optional.of(schema).ifPresent(optional -> tobeRemovedTables.addAll(optional.getAllTableNames()));
            tobeRemovedSchemas.add(each.toLowerCase());
        }
        Collection<MutableDataNodeRule> rules = database.getRuleMetaData().findRules(MutableDataNodeRule.class);
        for (String each : tobeRemovedTables) {
            removeDataNode(rules, each, tobeRemovedSchemas);
        }
        return Optional.of(new DropSchemaEvent(database.getName(), schemaNames));
    }
    
    private Collection<String> getSchemaNames(final DropSchemaStatement sqlStatement) {
        Collection<String> result = new LinkedList<>();
        for (IdentifierValue each : sqlStatement.getSchemaNames()) {
            result.add(each.getValue().toLowerCase());
        }
        return result;
    }
    
    private void removeDataNode(final Collection<MutableDataNodeRule> rules, final String tobeRemovedTable, final Collection<String> schemaNames) {
        for (MutableDataNodeRule each : rules) {
            each.remove(schemaNames, tobeRemovedTable);
        }
    }
    
    @Override
    public String getType() {
        return DropSchemaStatement.class.getName();
    }
}
