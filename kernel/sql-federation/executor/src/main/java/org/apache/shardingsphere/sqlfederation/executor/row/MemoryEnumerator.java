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

package org.apache.shardingsphere.sqlfederation.executor.row;

import lombok.SneakyThrows;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.util.ResultSetUtils;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.sqlfederation.optimizer.metadata.util.SQLFederationDataTypeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Memory enumerator.
 */
public final class MemoryEnumerator implements Enumerator<Object> {
    
    private final Collection<ShardingSphereRowData> rows;
    
    private final DatabaseType databaseType;
    
    private final Map<Integer, Class<?>> columnTypes;
    
    private Iterator<ShardingSphereRowData> iterator;
    
    private Object current;
    
    public MemoryEnumerator(final Collection<ShardingSphereRowData> rows, final Collection<ShardingSphereColumn> columns, final DatabaseType databaseType) {
        this.rows = rows;
        this.databaseType = databaseType;
        columnTypes = createColumnTypes(new ArrayList<>(columns));
        iterator = rows.iterator();
    }
    
    private Map<Integer, Class<?>> createColumnTypes(final List<ShardingSphereColumn> columns) {
        Map<Integer, Class<?>> result = new HashMap<>(columns.size(), 1F);
        for (int index = 0; index < columns.size(); index++) {
            result.put(index, SQLFederationDataTypeUtils.getSqlTypeClass(databaseType, columns.get(index)));
        }
        return result;
    }
    
    @Override
    public Object current() {
        return current;
    }
    
    @Override
    public boolean moveNext() {
        if (iterator.hasNext()) {
            current = convertToTargetType(iterator.next().getRows().toArray());
            return true;
        }
        current = null;
        iterator = rows.iterator();
        return false;
    }
    
    @SneakyThrows
    private Object[] convertToTargetType(final Object[] rows) {
        Object[] result = new Object[rows.length];
        for (int index = 0; index < rows.length; index++) {
            result[index] = ResultSetUtils.convertValue(rows[index], columnTypes.get(index));
        }
        return result;
    }
    
    @Override
    public void reset() {
    }
    
    @Override
    public void close() {
        iterator = rows.iterator();
        current = null;
    }
}
