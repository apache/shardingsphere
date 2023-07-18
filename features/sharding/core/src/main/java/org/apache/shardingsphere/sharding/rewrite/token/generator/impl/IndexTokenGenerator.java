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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import lombok.Setter;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.IndexToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.aware.ShardingRuleAware;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Index token generator.
 */
@Setter
public final class IndexTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, ShardingRuleAware, SchemaMetaDataAware {
    
    private ShardingRule shardingRule;
    
    private String databaseName;
    
    private Map<String, ShardingSphereSchema> schemas;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof IndexAvailable && !((IndexAvailable) sqlStatementContext).getIndexes().isEmpty();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        String defaultSchemaName = DatabaseTypeEngine.getDefaultSchemaName(sqlStatementContext.getDatabaseType(), databaseName);
        if (sqlStatementContext instanceof IndexAvailable) {
            for (IndexSegment each : ((IndexAvailable) sqlStatementContext).getIndexes()) {
                ShardingSphereSchema schema = each.getOwner().isPresent() ? schemas.get(each.getOwner().get().getIdentifier().getValue()) : schemas.get(defaultSchemaName);
                result.add(new IndexToken(each.getIndexName().getStartIndex(), each.getStopIndex(), each.getIndexName().getIdentifier(), sqlStatementContext, shardingRule, schema));
            }
        }
        return result;
    }
}
