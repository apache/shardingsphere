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

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.executor.exec.meta.Row;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;

import java.util.List;

/**
 * Executor with multi input Executor instance.
 */
@Getter
public class MultiExecutor extends AbstractExecutor implements Executor {
    
    private int queryResultIdx;
    
    private List<Executor> executors;
    
    public MultiExecutor(final List<Executor> executors, final ExecContext execContext) {
        super(execContext);
        Preconditions.checkArgument(!executors.isEmpty());
        this.executors = executors;
    }
    
    @Override
    public final boolean executeMove() {
        if (queryResultIdx >= executors.size()) {
            return false;
        }
        while (true) {
            Executor queryResult = executors.get(queryResultIdx);
            if (queryResult.moveNext()) {
                return true;
            }
            queryResultIdx++;
            if (queryResultIdx >= executors.size()) {
                return false;
            }
        }
    }
    
    @Override
    protected final void executeInit() {
        executors.forEach(Executor::init);
    }
    
    @Override
    public final QueryResultMetaData getMetaData() {
        return executors.get(0).getMetaData();
    }
    
    @Override
    public final Row current() {
        if (queryResultIdx >= executors.size()) {
            return null;
        }
        return executors.get(queryResultIdx).current();
    }
    
    @Override
    public final void close() {
        executors.forEach(Executor::close);
    }
}
