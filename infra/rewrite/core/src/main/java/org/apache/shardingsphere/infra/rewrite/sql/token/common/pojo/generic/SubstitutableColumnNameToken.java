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

package org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.generic;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Substitutable column name token.
 */
public final class SubstitutableColumnNameToken extends SQLToken implements Substitutable, RouteUnitAware {
    
    private static final String COLUMN_NAME_SPLITTER = ", ";
    
    @Getter
    private final int stopIndex;
    
    @Getter
    private final Collection<Projection> projections;
    
    private final QuoteCharacter quoteCharacter;
    
    @Getter
    private final DatabaseType databaseType;
    
    public SubstitutableColumnNameToken(final int startIndex, final int stopIndex, final Collection<Projection> projections, final DatabaseType databaseType) {
        super(startIndex);
        this.stopIndex = stopIndex;
        quoteCharacter = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getQuoteCharacter();
        this.projections = projections;
        this.databaseType = databaseType;
    }
    
    @Override
    public String toString(final RouteUnit routeUnit) {
        Map<String, String> logicAndActualTables = getLogicAndActualTables(routeUnit);
        StringBuilder result = new StringBuilder();
        int index = 0;
        for (Projection each : projections) {
            if (index > 0) {
                result.append(COLUMN_NAME_SPLITTER);
            }
            result.append(getColumnExpression(each, logicAndActualTables));
            index++;
        }
        return result.toString();
    }
    
    private Map<String, String> getLogicAndActualTables(final RouteUnit routeUnit) {
        if (null == routeUnit) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new CaseInsensitiveMap<>();
        for (RouteMapper each : routeUnit.getTableMappers()) {
            result.put(each.getLogicName(), each.getActualName());
        }
        return result;
    }
    
    private String getColumnExpression(final Projection projection, final Map<String, String> logicActualTableNames) {
        StringBuilder builder = new StringBuilder();
        if (projection instanceof ColumnProjection) {
            appendColumnProjection((ColumnProjection) projection, logicActualTableNames, builder);
        } else {
            builder.append(quoteCharacter.wrap(projection.getColumnLabel()));
        }
        return builder.toString();
    }
    
    private void appendColumnProjection(final ColumnProjection columnProjection, final Map<String, String> logicActualTableNames, final StringBuilder builder) {
        columnProjection.getLeftParentheses().ifPresent(optional -> builder.append("("));
        if (columnProjection.getOwner().isPresent()) {
            IdentifierValue owner = columnProjection.getOwner().get();
            String actualTableOwner = logicActualTableNames.getOrDefault(owner.getValue(), owner.getValue());
            builder.append(getValueWithQuoteCharacters(new IdentifierValue(actualTableOwner, owner.getQuoteCharacter()))).append('.');
        }
        builder.append(getValueWithQuoteCharacters(columnProjection.getName()));
        columnProjection.getRightParentheses().ifPresent(optional -> builder.append(")"));
        if (columnProjection.getAlias().isPresent()) {
            builder.append(" AS ").append(getValueWithQuoteCharacters(columnProjection.getAlias().get()));
        }
    }
    
    private String getValueWithQuoteCharacters(final IdentifierValue identifierValue) {
        return QuoteCharacter.NONE == identifierValue.getQuoteCharacter() ? identifierValue.getValue() : quoteCharacter.wrap(identifierValue.getValue());
    }
}
