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

package org.apache.shardingsphere.sqlfederation.row;

import org.apache.calcite.linq4j.Enumerator;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereRowData;

import java.util.Iterator;
import java.util.List;

/**
 * Memory enumerator.
 */
public final class MemoryEnumerator implements Enumerator<Object[]> {
    
    private final List<ShardingSphereRowData> rows;
    
    private Iterator<ShardingSphereRowData> rowDataIterator;
    
    private List<Object> current;
    
    public MemoryEnumerator(final List<ShardingSphereRowData> rows) {
        this.rows = rows;
        rowDataIterator = rows.iterator();
    }
    
    @Override
    public Object[] current() {
        return current.toArray();
    }
    
    @Override
    public boolean moveNext() {
        if (rowDataIterator.hasNext()) {
            current = rowDataIterator.next().getRows();
            return true;
        }
        current = null;
        rowDataIterator = rows.iterator();
        return false;
    }
    
    @Override
    public void reset() {
    }
    
    @Override
    public void close() {
        rowDataIterator = rows.iterator();
        current = null;
    }
}
