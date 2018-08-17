/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.executor.type.connection;

import io.shardingsphere.core.executor.BaseStatementUnit;
import io.shardingsphere.core.executor.ExecuteCallback;
import io.shardingsphere.core.executor.ExecutorEngine;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Connection strictly execute engine.
 *
 * @author panjuan
 */
public final class ConnectionStrictlyExecutorEngine extends ExecutorEngine {
    
    public ConnectionStrictlyExecutorEngine(final int executorSize) {
        super(executorSize);
    }
    
    @Override
    protected <T> List<T> getExecuteResults(final Collection<BaseStatementUnit> baseStatementUnits, final ExecuteCallback<T> executeCallback) throws Exception {
        return getShardingExecuteEngine().groupExecute(getBaseStatementUnitGroups(baseStatementUnits), executeCallback);
    }
    
    private Map<String, Collection<BaseStatementUnit>> getBaseStatementUnitGroups(final Collection<? extends BaseStatementUnit> baseStatementUnits) {
        Map<String, Collection<BaseStatementUnit>> result = new LinkedHashMap<>(baseStatementUnits.size(), 1);
        for (BaseStatementUnit each : baseStatementUnits) {
            String dataSourceName = each.getSqlExecutionUnit().getDataSource();
            if (!result.keySet().contains(dataSourceName)) {
                result.put(dataSourceName, new LinkedList<BaseStatementUnit>());
            }
            result.get(dataSourceName).add(each);
        }
        return result;
    }
}
