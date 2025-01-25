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

package org.apache.shardingsphere.mode.metadata.refresher.metadata.util;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.single.constant.SingleTableConstants;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Optional;

/**
 * Table refresh utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableRefreshUtils {
    
    /**
     * Get table name.
     *
     * @param tableIdentifierValue table identifier value
     * @param databaseType database type
     * @return table name
     */
    public static String getTableName(final IdentifierValue tableIdentifierValue, final DatabaseType databaseType) {
        return QuoteCharacter.NONE == tableIdentifierValue.getQuoteCharacter()
                ? new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().formatTableNamePattern(tableIdentifierValue.getValue())
                : tableIdentifierValue.getValue();
    }
    
    /**
     * Judge whether single table.
     *
     * @param tableName table name
     * @param database database
     * @return whether single table
     */
    public static boolean isSingleTable(final String tableName, final ShardingSphereDatabase database) {
        return database.getRuleMetaData().getAttributes(TableMapperRuleAttribute.class).stream().noneMatch(each -> each.getDistributedTableNames().contains(tableName));
    }
    
    /**
     * Judge whether to need refresh.
     *
     * @param ruleMetaData rule meta data
     * @param schemaName schema name
     * @param tableSegments table segments
     * @return need to refresh or not
     */
    public static boolean isNeedRefresh(final RuleMetaData ruleMetaData, final String schemaName, final Collection<SimpleTableSegment> tableSegments) {
        return tableSegments.stream().anyMatch(each -> isNeedRefresh(ruleMetaData, schemaName, each.getTableName().getIdentifier().getValue()));
    }
    
    /**
     * Judge whether to need refresh.
     *
     * @param ruleMetaData rule meta data
     * @param schemaName schema name
     * @param tableName table name
     * @return need to refresh or not
     */
    public static boolean isNeedRefresh(final RuleMetaData ruleMetaData, final String schemaName, final String tableName) {
        SingleRule singleRule = ruleMetaData.getSingleRule(SingleRule.class);
        Collection<String> singleTableNames = singleRule.getConfiguration().getTables();
        if (singleTableNames.contains(SingleTableConstants.ALL_TABLES) || singleTableNames.contains(SingleTableConstants.ALL_SCHEMA_TABLES)) {
            return false;
        }
        Optional<DataNode> dataNode = singleRule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class).findTableDataNode(schemaName, tableName);
        return dataNode.isPresent()
                && !singleTableNames.contains(joinDataNodeSegments(dataNode.get().getDataSourceName(), SingleTableConstants.ASTERISK))
                && !singleTableNames.contains(joinDataNodeSegments(dataNode.get().getDataSourceName(), SingleTableConstants.ALL_TABLES, SingleTableConstants.ASTERISK))
                && !singleTableNames.contains(joinDataNodeSegments(dataNode.get().getDataSourceName(), dataNode.get().getSchemaName(), SingleTableConstants.ASTERISK));
    }
    
    private static String joinDataNodeSegments(final String... segments) {
        return Joiner.on(".").join(segments);
    }
}
