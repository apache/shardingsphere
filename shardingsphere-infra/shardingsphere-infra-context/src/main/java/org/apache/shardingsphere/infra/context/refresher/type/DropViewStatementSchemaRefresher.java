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
import org.apache.shardingsphere.infra.metadata.database.schema.event.MetaDataRefreshedEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.rule.identifier.type.MutableDataNodeRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropViewStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Schema refresher for drop view statement.
 */
public final class DropViewStatementSchemaRefresher implements MetaDataRefresher<DropViewStatement> {
    
    @Override
    public Optional<MetaDataRefreshedEvent> refresh(final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                                                    final String schemaName, final DropViewStatement sqlStatement, final ConfigurationProperties props) throws SQLException {
        SchemaAlteredEvent event = new SchemaAlteredEvent(database.getName(), schemaName);
        sqlStatement.getViews().forEach(each -> {
            database.getSchema(schemaName).remove(each.getTableName().getIdentifier().getValue());
            event.getDroppedTables().add(each.getTableName().getIdentifier().getValue());
        });
        Collection<MutableDataNodeRule> rules = database.getRuleMetaData().findRules(MutableDataNodeRule.class);
        for (SimpleTableSegment each : sqlStatement.getViews()) {
            removeDataNode(rules, each, schemaName);
        }
        return Optional.of(event);
    }
    
    private void removeDataNode(final Collection<MutableDataNodeRule> rules, final SimpleTableSegment tobeRemovedSegment, final String schemaName) {
        for (MutableDataNodeRule each : rules) {
            each.remove(schemaName, tobeRemovedSegment.getTableName().getIdentifier().getValue());
        }
    }
    
    @Override
    public String getType() {
        return DropViewStatement.class.getName();
    }
}
