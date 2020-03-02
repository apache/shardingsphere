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

package org.apache.shardingsphere.sql.parser.relation.segment.table;

import lombok.ToString;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.TableSegmentsAvailable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Tables context.
 */
@ToString
public final class TablesContext {
    
    private final Collection<Table> tables;
    
    public TablesContext(final SQLStatement sqlStatement) {
        if (!(sqlStatement instanceof TableSegmentsAvailable)) {
            tables = Collections.emptyList();
            return;
        }
        Collection<TableSegment> tableSegments = getAllTables((TableSegmentsAvailable) sqlStatement);
        tables = new ArrayList<>(tableSegments.size());
        for (TableSegment each : tableSegments) {
            tables.add(new Table(each.getTableName().getIdentifier().getValue(), each.getAlias().orNull()));
        }
    }
    
    private Collection<TableSegment> getAllTables(final TableSegmentsAvailable sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return ((SelectStatement) sqlStatement).getTables();
        }
        if (sqlStatement instanceof UpdateStatement) {
            return ((UpdateStatement) sqlStatement).getTables();
        }
        if (sqlStatement instanceof DeleteStatement) {
            return ((DeleteStatement) sqlStatement).getTables();
        }
        return sqlStatement.getAllTables();
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
}
