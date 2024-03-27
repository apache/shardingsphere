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

package org.apache.shardingsphere.sqlfederation.executor.enumerator;

import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereRowData;
import org.apache.shardingsphere.sqlfederation.executor.utils.EnumeratorUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Memory row enumerator.
 */
public final class MemoryRowEnumerator implements Enumerator<Object> {
    
    private final Collection<ShardingSphereRowData> rows;
    
    private final Map<Integer, Class<?>> columnTypes;
    
    private Iterator<ShardingSphereRowData> iterator;
    
    private Object current;
    
    public MemoryRowEnumerator(final Collection<ShardingSphereRowData> rows, final Collection<ShardingSphereColumn> columns, final DatabaseType databaseType) {
        this.rows = rows;
        columnTypes = EnumeratorUtils.createColumnTypes(new ArrayList<>(columns), databaseType);
        iterator = rows.iterator();
    }
    
    @Override
    public Object current() {
        return current;
    }
    
    @Override
    public boolean moveNext() {
        if (iterator.hasNext()) {
            current = EnumeratorUtils.convertToTargetType(columnTypes, iterator.next().getRows().toArray());
            return true;
        }
        current = null;
        iterator = rows.iterator();
        return false;
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
