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

package org.apache.shardingsphere.infra.rewrite.sql.token.generator.generic;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.binder.type.RemoveAvailable;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.sql.parser.sql.common.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Remove token generator.
 */
public final class RemoveTokenGenerator implements CollectionSQLTokenGenerator<SQLStatementContext<?>> {
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        boolean containsRemoveSegment = false;
        if (sqlStatementContext instanceof RemoveAvailable) {
            containsRemoveSegment = !((RemoveAvailable) sqlStatementContext).getRemoveSegments().isEmpty();
        }
        boolean containsSchemaName = false;
        if (sqlStatementContext instanceof TableAvailable) {
            containsSchemaName = ((TableAvailable) sqlStatementContext).getTablesContext().getDatabaseName().isPresent();
        }
        boolean containsIndexSegment = false;
        if (sqlStatementContext instanceof IndexAvailable) {
            containsIndexSegment = !((IndexAvailable) sqlStatementContext).getIndexes().isEmpty();
        }
        return containsRemoveSegment || containsSchemaName || containsIndexSegment;
    }
    
    @Override
    public Collection<RemoveToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        Collection<RemoveToken> result = new LinkedList<>();
        if (sqlStatementContext instanceof RemoveAvailable && !((RemoveAvailable) sqlStatementContext).getRemoveSegments().isEmpty()) {
            result.addAll(generateRemoveAvailableSQLTokens(((RemoveAvailable) sqlStatementContext).getRemoveSegments()));
        }
        if (sqlStatementContext instanceof TableAvailable && ((TableAvailable) sqlStatementContext).getTablesContext().getDatabaseName().isPresent()) {
            result.addAll(generateTableAvailableSQLTokens((TableAvailable) sqlStatementContext));
        }
        if (sqlStatementContext instanceof IndexAvailable && !((IndexAvailable) sqlStatementContext).getIndexes().isEmpty()) {
            result.addAll(generateIndexAvailableSQLTokens((IndexAvailable) sqlStatementContext));
        }
        return result;
    }
    
    private Collection<RemoveToken> generateRemoveAvailableSQLTokens(final Collection<SQLSegment> removeSegments) {
        return removeSegments.stream().map(each -> new RemoveToken(each.getStartIndex(), each.getStopIndex())).collect(Collectors.toList());
    }
    
    private Collection<RemoveToken> generateTableAvailableSQLTokens(final TableAvailable tableAvailable) {
        Collection<RemoveToken> result = new LinkedList<>();
        for (SimpleTableSegment each : tableAvailable.getAllTables()) {
            if (!each.getOwner().isPresent()) {
                continue;
            }
            OwnerSegment owner = each.getOwner().get();
            int startIndex = owner.getOwner().isPresent() ? owner.getOwner().get().getStartIndex() : owner.getStartIndex();
            int stopIndex = owner.getOwner().isPresent() ? owner.getStartIndex() - 1 : each.getTableName().getStartIndex() - 1;
            result.add(new RemoveToken(startIndex, stopIndex));
        }
        return result;
    }
    
    private Collection<RemoveToken> generateIndexAvailableSQLTokens(final IndexAvailable indexAvailable) {
        Collection<RemoveToken> result = new LinkedList<>();
        for (IndexSegment each : indexAvailable.getIndexes()) {
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
