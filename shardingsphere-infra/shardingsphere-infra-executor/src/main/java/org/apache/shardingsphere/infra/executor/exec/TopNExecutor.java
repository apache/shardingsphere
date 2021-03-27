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

import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Executor that keep only top n elements in the heap.
 */
public class TopNExecutor extends LimitSortExecutor {
    
    private final PriorityQueue<Row> heap;
    
    public TopNExecutor(final Executor executor, final Comparator<Row> ordering, final int offset, final int fetch,
                        final ExecContext execContext) {
        super(executor, ordering, offset, fetch, execContext);
        heap = new PriorityQueue<>(fetch, ordering);
    }
    
    /**
     * initialize input interator.
     * @return initialized iterator.
     */
    @Override
    protected final Iterator<Row> initInputRowIterator() {
        while (getExecutor().moveNext()) {
            if (heap.size() > (getFetch() + getOffset())) {
                heap.poll();
            }
            Row row = getExecutor().current();
            heap.add(row);
        }
        Iterator<Row> inputRowIterator = new Iterator<Row>() {
            @Override
            public boolean hasNext() {
                return heap.size() > 0;
            }
    
            @Override
            public Row next() {
                return heap.poll();
            }
        };
        skipOffsetRows(inputRowIterator);
        return inputRowIterator;
    }
}
