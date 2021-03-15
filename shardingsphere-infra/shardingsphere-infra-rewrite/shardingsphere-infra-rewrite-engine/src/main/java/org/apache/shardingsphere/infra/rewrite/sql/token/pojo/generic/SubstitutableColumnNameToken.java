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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Substitutable column name token.
 */
@EqualsAndHashCode
public final class SubstitutableColumnNameToken extends SQLToken implements Substitutable, RouteUnitAware {
    
    @Getter
    private final int stopIndex;
    
    private final String columnName;

    private Map<String, List<SubstitutableColumn>> tableColumns = new HashMap<>();

    private Optional<String> owner;

    public SubstitutableColumnNameToken(final int startIndex, final int stopIndex, final String columnName) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.columnName = columnName;

    }

    public SubstitutableColumnNameToken(final int startIndex, final int stopIndex, final Optional<String> owner) {
        super(startIndex);
        this.stopIndex = stopIndex;
        this.columnName = "";
        this.owner = owner;
    }

    public SubstitutableColumnNameToken put(String tableName, SubstitutableColumn column) {
        tableColumns.computeIfAbsent(tableName, key -> new LinkedList()).add(column);
        return this;
    }

    @Override
    public String toString(RouteUnit routeUnit) {
        List<String> actualColumns = new ArrayList<>();
        for (Map.Entry<String, List<SubstitutableColumn>> each : tableColumns.entrySet()) {
            String actualTableName = routeUnit.getActualTableNames(each.getKey()).iterator().next();
            List<String> tableActualColumns = each.getValue().stream().map(column -> {
                String actualColumnName = column.getQuoteCharacter().wrap(column.getName());
                if(column.getAlias().isPresent()){
                    actualColumnName = actualColumnName + " AS " + column.getAlias().get();
                }
                if(owner.isPresent()){
                    if(column.getTableName().isPresent() && !column.getTableName().get().equals(owner.get())){
                        return Joiner.on(".").join(column.getQuoteCharacter().wrap(owner.get()), actualColumnName);
                    }
                    return actualColumnName;
                }
                if(column.getOwner().isPresent()){
                    if(column.getTableName().isPresent() && !column.getTableName().get().equals(column.getOwner().get())){
                        return Joiner.on(".").join(column.getQuoteCharacter().wrap(column.getOwner().get()), actualColumnName);
                    }else {
                        return Joiner.on(".").join(column.getQuoteCharacter().wrap(actualTableName), actualColumnName);
                    }
                }
                return actualColumnName;
            }).collect(Collectors.toList());

            actualColumns.addAll(tableActualColumns);
        }
        return Joiner.on(", ").join(actualColumns);
    }
}
