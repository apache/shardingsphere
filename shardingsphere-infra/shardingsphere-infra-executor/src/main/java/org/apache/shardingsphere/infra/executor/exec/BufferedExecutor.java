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

package org.apache.shardingsphere.infra.executor.exec;

import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.util.ArrayList;
import java.util.List;

/**
 * This <code>Executor</code> is designed to buffer all rows from another Executor instance. This can be used in 
 * nested loop join, see {@link NestedLoopJoinExecutor}
 */
public final class BufferedExecutor extends SingleExecutor {
    
    private int idx;
    
    private final List<Row> rows;
    
    public BufferedExecutor(final Executor executor, final ExecContext execContext) {
        super(executor, execContext);
        rows = new ArrayList<>();
    }
    
    @Override
    protected void doInit() {
        bufferRows();
    }
    
    @Override
    public QueryResultMetaData getMetaData() {
        return getExecutor().getMetaData();
    }
    
    @Override
    public Row current() {
        return rows.get(idx - 1);
    }
    
    @Override
    public void reset() {
        idx = 0;
    }
    
    @Override
    public boolean executeMove() {
        return idx++ < rows.size();
    }
    
    private void bufferRows() {
        while (getExecutor().moveNext()) {
            Row row = getExecutor().current();
            rows.add(row);
        }
    }
}
