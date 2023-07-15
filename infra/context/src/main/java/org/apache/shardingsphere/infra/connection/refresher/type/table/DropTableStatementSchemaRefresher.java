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

package org.apache.shardingsphere.infra.connection.refresher.type.table;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.connection.refresher.MetaDataRefresher;
import org.apache.shardingsphere.infra.connection.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.rule.identifier.type.TableContainedRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropTableStatement;

import java.util.Collection;

/**
 * Schema refresher for drop table statement.
 */
public final class DropTableStatementSchemaRefresher implements MetaDataRefresher<DropTableStatement> {
    
    @Override
    public void refresh(final ModeContextManager modeContextManager, final ShardingSphereDatabase database, final Collection<String> logicDataSourceNames,
                        final String schemaName, final DropTableStatement sqlStatement, final ConfigurationProperties props) {
        AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO = new AlterSchemaMetaDataPOJO(database.getName(), schemaName);
        sqlStatement.getTables().forEach(each -> alterSchemaMetaDataPOJO.getDroppedTables().add(each.getTableName().getIdentifier().getValue()));
        ShardingSphereRuleMetaData ruleMetaData = database.getRuleMetaData();
        boolean isRuleRefreshRequired = TableRefreshUtils.isRuleRefreshRequired(ruleMetaData, schemaName, sqlStatement.getTables());
        modeContextManager.alterSchemaMetaData(alterSchemaMetaDataPOJO);
        for (SimpleTableSegment each : sqlStatement.getTables()) {
            if (isRuleRefreshRequired && isSingleTable(each.getTableName().getIdentifier().getValue(), ruleMetaData)) {
                modeContextManager.alterRuleConfiguration(database.getName(), ruleMetaData.getConfigurations());
                break;
            }
        }
    }
    
    private boolean isSingleTable(final String tableName, final ShardingSphereRuleMetaData ruleMetaData) {
        return ruleMetaData.findRules(TableContainedRule.class).stream().noneMatch(each -> each.getDistributedTableMapper().contains(tableName));
    }
    
    @Override
    public String getType() {
        return DropTableStatement.class.getName();
    }
}
