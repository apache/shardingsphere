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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.vertx;

import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.vertx.VertxMySQLQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.vertx.VertxQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Vert.x executor callback.
 */
public final class VertxExecutorCallback implements ExecutorCallback<VertxExecutionUnit, Future<ExecuteResult>> {
    
    @Override
    public Collection<Future<ExecuteResult>> execute(final Collection<VertxExecutionUnit> inputs, final boolean isTrunkThread, final Map<String, Object> dataMap) {
        List<Future<ExecuteResult>> result = new ArrayList<>(inputs.size());
        for (VertxExecutionUnit each : inputs) {
            Future<RowSet<Row>> future = each.getStorageResource().compose(preparedQuery -> preparedQuery.execute(Tuple.from(each.getExecutionUnit().getSqlUnit().getParameters())));
            result.add(future.compose(this::handleResult));
        }
        return result;
    }
    
    private Future<ExecuteResult> handleResult(final RowSet<Row> rowSet) {
        if (null == rowSet.columnDescriptors()) {
            return Future.succeededFuture(new UpdateResult(rowSet.rowCount(), getGeneratedKey(rowSet)));
        }
        // TODO Decoupling MySQL implementations
        List<ColumnDefinition> columnDefinitions = new ArrayList<>(rowSet.columnDescriptors().size());
        rowSet.columnDescriptors().forEach(each -> columnDefinitions.add((ColumnDefinition) each));
        return Future.succeededFuture(new VertxQueryResult(new VertxMySQLQueryResultMetaData(columnDefinitions), rowSet.iterator()));
    }
    
    private long getGeneratedKey(final RowSet<Row> rowSet) {
        Long result = rowSet.property(MySQLClient.LAST_INSERTED_ID);
        return null == result ? 0L : result;
    }
}
