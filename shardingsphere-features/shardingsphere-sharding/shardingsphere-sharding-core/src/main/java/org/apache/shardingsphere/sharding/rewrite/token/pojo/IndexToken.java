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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateIndexStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtil;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Map;
import java.util.Optional;

/**
 * Index token.
 */
public final class IndexToken extends SQLToken implements Substitutable, RouteUnitAware {
    
    @Getter
    private final int stopIndex;
    
    private final IdentifierValue identifier;
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final ShardingRule shardingRule;
    
    private final ShardingSphereSchema schema;
    
    public IndexToken(final int startIndex, final int stopIndex, final IdentifierValue identifier,
                      final SQLStatementContext<?> sqlStatementContext, final ShardingRule shardingRule, final ShardingSphereSchema schema) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.identifier = identifier;
        this.sqlStatementContext = sqlStatementContext;
        this.shardingRule = shardingRule;
        this.schema = schema;
    }
    
    @Override
    public String toString(final RouteUnit routeUnit) {
        String quotedIndexName = identifier.getQuoteCharacter().wrap(getIndexValue(routeUnit));
        return isGeneratedIndex() ? " " + quotedIndexName + " " : quotedIndexName;
    }
    
    private boolean isGeneratedIndex() {
        return sqlStatementContext instanceof CreateIndexStatementContext && ((CreateIndexStatementContext) sqlStatementContext).isGeneratedIndex();
    }
    
    private String getIndexValue(final RouteUnit routeUnit) {
        Map<String, String> logicAndActualTables = TokenUtil.getLogicAndActualTables(routeUnit, sqlStatementContext, shardingRule);
        String actualTableName = findLogicTableNameFromMetaData(identifier.getValue()).map(logicAndActualTables::get)
                .orElseGet(() -> logicAndActualTables.values().stream().findFirst().orElse(null));
        return IndexMetaDataUtil.getActualIndexName(identifier.getValue(), actualTableName);
    }
    
    private Optional<String> findLogicTableNameFromMetaData(final String logicIndexName) {
        for (String each : schema.getAllTableNames()) {
            if (schema.get(each).getIndexes().containsKey(logicIndexName)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
}
