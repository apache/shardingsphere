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

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.IndexToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.IndexSQLStatementAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

/**
 * Sharding index token generator.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
@Setter
public final class ShardingIndexTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext>, SchemaMetaDataAware {
    
    private final ShardingRule rule;
    
    private Map<String, ShardingSphereSchema> schemas;
    
    private ShardingSphereSchema defaultSchema;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext.getSqlStatement().getAttributes().findAttribute(IndexSQLStatementAttribute.class).map(optional -> !optional.getIndexes().isEmpty()).orElse(false)
                && new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData().getIndexOption().isSchemaUniquenessLevel();
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        for (IndexSegment each : sqlStatementContext.getSqlStatement().getAttributes()
                .findAttribute(IndexSQLStatementAttribute.class).map(IndexSQLStatementAttribute::getIndexes).orElse(Collections.emptyList())) {
            ShardingSphereSchema schema = each.getOwner().isPresent() ? schemas.get(each.getOwner().get().getIdentifier().getValue()) : defaultSchema;
            result.add(new IndexToken(each.getIndexName().getStartIndex(), each.getStopIndex(), each.getIndexName().getIdentifier(), sqlStatementContext, rule, schema));
        }
        return result;
    }
}
