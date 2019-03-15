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

package org.apache.shardingsphere.core.parse.parser.context.table;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Tables.
 * 
 * @author zhangliang
 */
@ToString
public final class Tables {
    
    private final List<Table> tables = new ArrayList<>();
    
    /**
     * Add table.
     * 
     * @param table table
     */
    public void add(final Table table) {
        tables.add(table);
    }
    
    /**
     * Judge table is empty or not.
     *
     * @return table is empty or not
     */
    public boolean isEmpty() {
        return tables.isEmpty();
    }
    
    /**
     * Judge is same table or not.
     *
     * @return is same table or not
     */
    public boolean isSameTable() {
        Set<String> tableNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Table each : tables) {
            tableNames.add(each.getName());
        }
        return 1 == tableNames.size();
    }
    
    /**
     * Judge is single table or not.
     * 
     * @return is single table or not
     */
    public boolean isSingleTable() {
        return 1 == tables.size();
    }
    
    /**
     * Get single table name.
     *
     * @return single table name
     */
    public String getSingleTableName() {
        Preconditions.checkArgument(!isEmpty());
        return tables.get(0).getName();
    }
    
    /**
     * Get table names.
     * 
     * @return table names
     */
    public Collection<String> getTableNames() {
        Collection<String> result = new LinkedHashSet<>(tables.size(), 1);
        for (Table each : tables) {
            result.add(each.getName());
        }
        return result;
    }
    
    /**
     * Find table via table name or alias.
     * 
     * @param tableNameOrAlias table name or alias
     * @return table
     */
    public Optional<Table> find(final String tableNameOrAlias) {
        Optional<Table> tableFromName = findTableFromName(tableNameOrAlias);
        return tableFromName.isPresent() ? tableFromName : findTableFromAlias(tableNameOrAlias);
    }
    
    private Optional<Table> findTableFromName(final String name) {
        for (Table each : tables) {
            if (each.getName().equals(name)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private Optional<Table> findTableFromAlias(final String alias) {
        for (Table each : tables) {
            if (each.getAlias().isPresent() && each.getAlias().get().equalsIgnoreCase(alias)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
}
