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

package org.apache.shardingsphere.infra.executor.sql.federate.original.sql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.federate.original.table.FilterableTableScanContext;
import org.apache.shardingsphere.infra.federation.metadata.FederationTableMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Filterable SQL generator.
 */
@RequiredArgsConstructor
public final class FilterableSQLGenerator {
    
    private final FederationTableMetaData tableMetaData;
    
    private final FilterableTableScanContext scanContext;
    
    private final QuoteCharacter quoteCharacter;
    
    /**
     * Generate federation SQL.
     * 
     * @param actualTableName actual table name
     * @return generated federation SQL
     */
    public String generate(final String actualTableName) {
        String projections = getQuotedProjections(tableMetaData, scanContext, quoteCharacter);
        String table = getQuotedTable(actualTableName, quoteCharacter);
        // TODO generate SQL with filters
        return String.format("SELECT %s FROM %s", projections, table);
    }
    
    private String getQuotedProjections(final FederationTableMetaData tableMetaData, final FilterableTableScanContext scanContext, final QuoteCharacter quoteCharacter) {
        Collection<String> actualColumnNames = null == scanContext.getProjects()
                ? tableMetaData.getColumnNames() : Arrays.stream(scanContext.getProjects()).mapToObj(tableMetaData.getColumnNames()::get).collect(Collectors.toList());
        return actualColumnNames.stream().map(quoteCharacter::wrap).collect(Collectors.joining(", "));
    }
    
    private String getQuotedTable(final String actualTableName, final QuoteCharacter quoteCharacter) {
        return quoteCharacter.wrap(actualTableName);
    }
}
