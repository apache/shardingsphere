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

package org.apache.shardingsphere.infra.connection.refresher.util;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.oracle.type.OracleDatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.api.constant.SingleTableConstants;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Table refresh utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableRefreshUtils {
    
    /**
     * Get table name.
     *
     * @param databaseType database type
     * @param identifierValue identifier value
     * @return table name
     */
    public static String getTableName(final DatabaseType databaseType, final IdentifierValue identifierValue) {
        return databaseType instanceof OracleDatabaseType && QuoteCharacter.NONE == identifierValue.getQuoteCharacter() ? identifierValue.getValue().toUpperCase() : identifierValue.getValue();
    }
    
    /**
     * Judge whether single table.
     *
     * @param tableName table name
     * @param database database
     * @return whether single table
     */
    public static boolean isSingleTable(final String tableName, final ShardingSphereDatabase database) {
        return database.getRuleMetaData().getAttributes(TableMapperRuleAttribute.class).stream().noneMatch(each -> each.getDistributedTableMapper().contains(tableName));
    }
    
    /**
     * Judge whether the rule need to be refreshed.
     *
     * @param ruleMetaData rule meta data
     * @param schemaName schema name
     * @param tableSegments table segments
     * @return whether the rule need to be refreshed
     */
    public static boolean isRuleRefreshRequired(final RuleMetaData ruleMetaData, final String schemaName, final Collection<SimpleTableSegment> tableSegments) {
        for (SimpleTableSegment each : tableSegments) {
            if (isRuleRefreshRequired(ruleMetaData, schemaName, each.getTableName().getIdentifier().getValue())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Judge whether the rule need to be refreshed.
     * 
     * @param ruleMetaData rule meta data
     * @param schemaName schema name
     * @param tableName table name
     * @return whether the rule need to be refreshed
     */
    public static boolean isRuleRefreshRequired(final RuleMetaData ruleMetaData, final String schemaName, final String tableName) {
        Collection<ShardingSphereRule> rules = new LinkedList<>();
        for (ShardingSphereRule each : ruleMetaData.getRules()) {
            each.getAttributes().findAttribute(MutableDataNodeRuleAttribute.class).ifPresent(optional -> rules.add(each));
        }
        if (rules.isEmpty()) {
            return false;
        }
        ShardingSphereRule rule = rules.iterator().next();
        RuleConfiguration ruleConfig = rule.getConfiguration();
        if (!(ruleConfig instanceof SingleRuleConfiguration)) {
            return false;
        }
        Collection<String> tablesConfig = ((SingleRuleConfiguration) ruleConfig).getTables();
        if (tablesConfig.contains(SingleTableConstants.ALL_TABLES) || tablesConfig.contains(SingleTableConstants.ALL_SCHEMA_TABLES)) {
            return false;
        }
        Optional<DataNode> dataNode = rule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).findTableDataNode(schemaName, tableName);
        if (!dataNode.isPresent()) {
            return false;
        }
        DataNode actualNode = dataNode.get();
        return !tablesConfig.contains(joinDataNodeSegments(actualNode.getDataSourceName(), SingleTableConstants.ASTERISK))
                && !tablesConfig.contains(joinDataNodeSegments(actualNode.getDataSourceName(), SingleTableConstants.ASTERISK, SingleTableConstants.ASTERISK))
                && !tablesConfig.contains(joinDataNodeSegments(actualNode.getDataSourceName(), actualNode.getSchemaName(), SingleTableConstants.ASTERISK));
    }
    
    private static String joinDataNodeSegments(final String... segments) {
        return Joiner.on(".").join(segments);
    }
}
