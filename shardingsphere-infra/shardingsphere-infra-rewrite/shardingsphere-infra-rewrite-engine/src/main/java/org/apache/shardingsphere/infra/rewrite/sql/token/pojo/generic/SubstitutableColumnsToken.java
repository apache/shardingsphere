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

import com.google.common.base.Joiner;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.RouteUnitAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.Substitutable;
import org.apache.shardingsphere.infra.route.context.RouteUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Substitutable column name token.
 */
@EqualsAndHashCode
public final class SubstitutableColumnsToken extends SQLToken implements Substitutable, RouteUnitAware {
    
    @Getter
    private final int stopIndex;
    
    private List<SubstitutableColumn> substitutableColumns = new LinkedList<>();

    public SubstitutableColumnsToken(final int startIndex, final int stopIndex, final SubstitutableColumn... columns) {
        super(startIndex);
        this.stopIndex = stopIndex;
        addColumn(columns);
    }

    /**
     * add SubstitutableColumn.
     * @param columns Substitutable columns
     */
    public void addColumn(final SubstitutableColumn... columns) {
        Collections.addAll(substitutableColumns, columns);
    }

    @Override
    public String toString(final RouteUnit routeUnit) {
        List<String> actualColumnNames = new ArrayList<>();
        Map<String, List<SubstitutableColumn>> tableColumns = substitutableColumns.stream().collect(Collectors.groupingBy(SubstitutableColumn::getTableName));
        for (Map.Entry<String, List<SubstitutableColumn>> each : tableColumns.entrySet()) {
            Set<String> actualTableNames = routeUnit.getActualTableNames(each.getKey());
            String actualTableName = actualTableNames.isEmpty() ? each.getKey().toLowerCase() : actualTableNames.iterator().next();
            List<String> tableActualColumns = each.getValue().stream().map(column -> {
                String actualColumnName = column.getQuoteCharacter().wrap(column.getName());
                if (column.getAlias().isPresent()) {
                    actualColumnName = actualColumnName + " AS " + column.getAlias().get();
                }
                if (Objects.nonNull(column.getOwner()) && !column.getOwner().isEmpty()) {
                    if (Objects.nonNull(column.getTableName()) && !column.getTableName().equals(column.getOwner())) {
                        return Joiner.on(".").join(column.getQuoteCharacter().wrap(column.getOwner()), actualColumnName);
                    }
                    return Joiner.on(".").join(column.getQuoteCharacter().wrap(actualTableName), actualColumnName);
                }
                return actualColumnName;
            }).collect(Collectors.toList());

            actualColumnNames.addAll(tableActualColumns);
        }
        return actualColumnNames.isEmpty() ? "" : Joiner.on(", ").join(actualColumnNames);
    }

    @Override
    public String toString() {
        List<String> actualColumnNames = new ArrayList<>();
        for (SubstitutableColumn column : substitutableColumns) {
            String actualColumnName = column.getQuoteCharacter().wrap(column.getName());
            if (column.getAlias().isPresent()) {
                actualColumnName = actualColumnName + " AS " + column.getAlias().get();
            }
            if (Objects.nonNull(column.getOwner()) && !column.getOwner().isEmpty()) {
                if (Objects.nonNull(column.getTableName()) && !column.getTableName().equals(column.getOwner())) {
                    actualColumnNames.add(Joiner.on(".").join(column.getQuoteCharacter().wrap(column.getOwner()), actualColumnName));
                } else {
                    actualColumnNames.add(Joiner.on(".").join(column.getQuoteCharacter().wrap(column.getTableName()), actualColumnName));
                }
                continue;
            }
            actualColumnNames.add(actualColumnName);
        }
        return actualColumnNames.isEmpty() ? "" : Joiner.on(", ").join(actualColumnNames);
    }
}
