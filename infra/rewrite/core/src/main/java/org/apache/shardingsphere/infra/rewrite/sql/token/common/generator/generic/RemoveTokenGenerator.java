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

package org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.generic;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic.RemoveToken;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.IndexSQLStatementAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Remove token generator.
 */
public final class RemoveTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext> {
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        if (DatabaseTypedSPILoader.findService(DialectToBeRemovedSegmentsProvider.class, sqlStatementContext.getSqlStatement().getDatabaseType())
                .map(optional -> !optional.getToBeRemovedSQLSegments(sqlStatementContext.getSqlStatement()).isEmpty()).orElse(false)) {
            return true;
        }
        if (!sqlStatementContext.getTablesContext().getSimpleTables().isEmpty()) {
            return true;
        }
        return sqlStatementContext.getSqlStatement().getAttributes().findAttribute(IndexSQLStatementAttribute.class).map(optional -> !optional.getIndexes().isEmpty()).orElse(false);
    }
    
    @Override
    public Collection<SQLToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<SQLToken> result = new LinkedList<>();
        Collection<SQLSegment> toBeRemovedSQLSegments = DatabaseTypedSPILoader.findService(DialectToBeRemovedSegmentsProvider.class, sqlStatementContext.getSqlStatement().getDatabaseType())
                .map(optional -> optional.getToBeRemovedSQLSegments(sqlStatementContext.getSqlStatement())).orElse(Collections.emptyList());
        if (!toBeRemovedSQLSegments.isEmpty()) {
            result.addAll(generateRemoveAvailableSQLTokens(toBeRemovedSQLSegments));
        }
        if (!sqlStatementContext.getTablesContext().getSimpleTables().isEmpty()) {
            result.addAll(generateTableAvailableSQLTokens(sqlStatementContext));
        }
        if (sqlStatementContext.getSqlStatement().getAttributes().findAttribute(IndexSQLStatementAttribute.class).map(optional -> !optional.getIndexes().isEmpty()).orElse(false)) {
            result.addAll(generateIndexContextAvailableSQLTokens(sqlStatementContext));
        }
        return result;
    }
    
    private Collection<RemoveToken> generateRemoveAvailableSQLTokens(final Collection<SQLSegment> removeSegments) {
        return removeSegments.stream().map(each -> new RemoveToken(each.getStartIndex(), each.getStopIndex())).collect(Collectors.toList());
    }
    
    private Collection<RemoveToken> generateTableAvailableSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<RemoveToken> result = new LinkedList<>();
        for (SimpleTableSegment each : sqlStatementContext.getTablesContext().getSimpleTables()) {
            if (!each.getOwner().isPresent()) {
                continue;
            }
            DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData();
            OwnerSegment ownerSegment = each.getOwner().get();
            if (dialectDatabaseMetaData.getSchemaOption().getDefaultSchema().isPresent()) {
                ownerSegment.getOwner().ifPresent(optional -> result.add(new RemoveToken(optional.getStartIndex(), ownerSegment.getStartIndex() - 1)));
            } else {
                result.add(new RemoveToken(ownerSegment.getStartIndex(), each.getTableName().getStartIndex() - 1));
            }
        }
        return result;
    }
    
    private Collection<RemoveToken> generateIndexContextAvailableSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<RemoveToken> result = new LinkedList<>();
        for (IndexSegment each : sqlStatementContext.getSqlStatement().getAttributes()
                .findAttribute(IndexSQLStatementAttribute.class).map(IndexSQLStatementAttribute::getIndexes).orElse(Collections.emptyList())) {
            if (!each.getOwner().isPresent()) {
                continue;
            }
            OwnerSegment owner = each.getOwner().get();
            int startIndex = owner.getOwner().isPresent() ? owner.getOwner().get().getStartIndex() : owner.getStartIndex();
            int stopIndex = owner.getOwner().isPresent() ? owner.getStartIndex() - 1 : each.getStartIndex() - 1;
            result.add(new RemoveToken(startIndex, stopIndex));
        }
        return result;
    }
}
