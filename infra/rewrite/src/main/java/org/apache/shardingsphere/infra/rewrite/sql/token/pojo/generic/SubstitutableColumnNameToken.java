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

package org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.enums.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Substitutable column name token.
 */
@EqualsAndHashCode(callSuper = false)
public final class SubstitutableColumnNameToken extends SQLToken implements Substitutable, RouteUnitAware {
    
    private static final String COLUMN_NAME_SPLITTER = ", ";
    
    @Getter
    private final int stopIndex;
    
    private final Collection<Projection> projections;
    
    private final boolean lastColumn;
    
    private final QuoteCharacter quoteCharacter;
    
    public SubstitutableColumnNameToken(final int startIndex, final int stopIndex, final Collection<Projection> projections) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.lastColumn = false;
        this.quoteCharacter = QuoteCharacter.NONE;
        this.projections = projections;
    }
    
    public SubstitutableColumnNameToken(final int startIndex, final int stopIndex, final Collection<Projection> projections, final boolean lastColumn) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.lastColumn = lastColumn;
        this.quoteCharacter = QuoteCharacter.NONE;
        this.projections = projections;
    }
    
    public SubstitutableColumnNameToken(final int startIndex, final int stopIndex, final Collection<Projection> projections, final QuoteCharacter quoteCharacter) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.lastColumn = false;
        this.quoteCharacter = quoteCharacter;
        this.projections = projections;
    }
    
    @Override
    public String toString(final RouteUnit routeUnit) {
        Map<String, String> logicAndActualTables = new HashMap<>();
        if (null != routeUnit) {
            logicAndActualTables.putAll(getLogicAndActualTables(routeUnit));
        }
        StringBuilder result = new StringBuilder();
        int count = 0;
        for (Projection each : projections) {
            if (0 == count && !lastColumn) {
                result.append(getColumnExpression(each, logicAndActualTables));
            } else {
                result.append(COLUMN_NAME_SPLITTER).append(getColumnExpression(each, logicAndActualTables));
            }
            count++;
        }
        return result.toString();
    }
    
    private Map<String, String> getLogicAndActualTables(final RouteUnit routeUnit) {
        if (null == routeUnit) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (RouteMapper each : routeUnit.getTableMappers()) {
            result.put(each.getLogicName().toLowerCase(), each.getActualName());
        }
        return result;
    }
    
    private String getColumnExpression(final Projection projection, final Map<String, String> logicActualTableNames) {
        StringBuilder builder = new StringBuilder();
        if (projection instanceof ColumnProjection) {
            appendColumnProjection((ColumnProjection) projection, logicActualTableNames, builder);
        } else {
            // TODO use alias quoteCharacter to avoid oracle rewrite error
            builder.append(quoteCharacter.wrap(projection.getColumnLabel()));
        }
        return builder.toString();
    }
    
    private static void appendColumnProjection(final ColumnProjection columnProjection, final Map<String, String> logicActualTableNames, final StringBuilder builder) {
        if (columnProjection.getOwner().isPresent()) {
            Optional<IdentifierValue> owner = columnProjection.getOwner();
            String lowerCaseOwner = owner.get().getValue().toLowerCase();
            builder.append(owner.get().getQuoteCharacter().wrap(logicActualTableNames.getOrDefault(lowerCaseOwner, owner.get().getValue()))).append('.');
        }
        builder.append(columnProjection.getName().getValueWithQuoteCharacters());
        if (columnProjection.getAlias().isPresent()) {
            builder.append(" AS ").append(columnProjection.getAlias().get().getValueWithQuoteCharacters());
        }
    }
}
