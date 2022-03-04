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

import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Substitutable column name token.
 */
@EqualsAndHashCode(callSuper = false)
public final class SubstitutableColumnNameToken extends SQLToken implements Substitutable, RouteUnitAware {
    
    private static final String COLUMN_NAME_SPLITTER = ", ";
    
    @Getter
    private final int stopIndex;
    
    private final Collection<ColumnProjection> projections;
    
    private final boolean lastColumn;
    
    private final QuoteCharacter quoteCharacter;
    
    public SubstitutableColumnNameToken(final int startIndex, final int stopIndex, final Collection<ColumnProjection> projections) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.lastColumn = false;
        this.quoteCharacter = QuoteCharacter.NONE;
        this.projections = projections;
    }
    
    public SubstitutableColumnNameToken(final int startIndex, final int stopIndex, final Collection<ColumnProjection> projections, final boolean lastColumn) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.lastColumn = lastColumn;
        this.quoteCharacter = QuoteCharacter.NONE;
        this.projections = projections;
    }
    
    public SubstitutableColumnNameToken(final int startIndex, final int stopIndex, final Collection<ColumnProjection> projections, final QuoteCharacter quoteCharacter) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.lastColumn = false;
        this.quoteCharacter = quoteCharacter;
        this.projections = projections;
    }
    
    @Override
    public String toString(final RouteUnit routeUnit) {
        Map<String, String> logicAndActualTables = getLogicAndActualTables(routeUnit);
        StringBuilder builder = lastColumn ? new StringBuilder(COLUMN_NAME_SPLITTER) : new StringBuilder();
        for (ColumnProjection each : projections) {
            builder.append(getColumnName(each, logicAndActualTables)).append(COLUMN_NAME_SPLITTER);
        }
        return builder.substring(0, builder.length() - COLUMN_NAME_SPLITTER.length());
    }
    
    private Map<String, String> getLogicAndActualTables(final RouteUnit routeUnit) {
        Map<String, String> result = new LinkedHashMap<>();
        for (RouteMapper each : routeUnit.getTableMappers()) {
            result.put(each.getLogicName(), each.getActualName());
        }
        return result;
    }
    
    private String getColumnName(final ColumnProjection columnProjection, final Map<String, String> logicActualTableNames) {
        StringBuilder builder = new StringBuilder();
        String owner = columnProjection.getOwner();
        if (!Strings.isNullOrEmpty(owner)) {
            builder.append(quoteCharacter.wrap(logicActualTableNames.getOrDefault(owner, owner))).append(".");
        }
        builder.append(quoteCharacter.wrap(columnProjection.getName()));
        if (columnProjection.getAlias().isPresent()) {
            builder.append(" AS ").append(quoteCharacter.wrap(columnProjection.getAlias().get()));
        }
        return builder.toString();
    }
}
