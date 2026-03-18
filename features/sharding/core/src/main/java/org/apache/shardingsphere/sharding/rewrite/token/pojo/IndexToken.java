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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Map;
import java.util.Optional;

/**
 * Index token.
 */
public final class IndexToken extends SQLToken implements Substitutable, RouteUnitAware {
    
    @Getter
    private final int stopIndex;
    
    private final IdentifierValue identifier;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final ShardingRule rule;
    
    private final ShardingSphereSchema schema;
    
    public IndexToken(final int startIndex, final int stopIndex, final IdentifierValue identifier,
                      final SQLStatementContext sqlStatementContext, final ShardingRule shardingRule, final ShardingSphereSchema schema) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.identifier = identifier;
        this.sqlStatementContext = sqlStatementContext;
        rule = shardingRule;
        this.schema = schema;
    }
    
    @Override
    public String toString(final RouteUnit routeUnit) {
        String quotedIndexName = identifier.getQuoteCharacter().wrap(getIndexValue(routeUnit));
        return isGeneratedIndex() ? " " + quotedIndexName + " " : quotedIndexName;
    }
    
    private boolean isGeneratedIndex() {
        return sqlStatementContext.getSqlStatement() instanceof CreateIndexStatement && null == ((CreateIndexStatement) sqlStatementContext.getSqlStatement()).getIndex();
    }
    
    private String getIndexValue(final RouteUnit routeUnit) {
        Optional<String> logicTableName = findLogicTableNameFromMetaData(identifier.getValue());
        if (logicTableName.isPresent() && !rule.isShardingTable(logicTableName.get())) {
            return identifier.getValue();
        }
        Map<String, String> logicAndActualTables = ShardingTokenUtils.getLogicAndActualTableMap(routeUnit, sqlStatementContext, rule);
        String actualTableName = logicTableName.map(logicAndActualTables::get).orElseGet(() -> logicAndActualTables.isEmpty() ? null : logicAndActualTables.values().iterator().next());
        return IndexMetaDataUtils.getActualIndexName(identifier.getValue(), actualTableName);
    }
    
    private Optional<String> findLogicTableNameFromMetaData(final String logicIndexName) {
        for (ShardingSphereTable each : schema.getAllTables()) {
            if (each.containsIndex(logicIndexName)) {
                return Optional.of(each.getName());
            }
        }
        return Optional.empty();
    }
}
