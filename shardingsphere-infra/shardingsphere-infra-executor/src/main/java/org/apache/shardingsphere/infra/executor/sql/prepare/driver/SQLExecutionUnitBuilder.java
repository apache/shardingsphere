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

package org.apache.shardingsphere.infra.executor.sql.prepare.driver;

import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.DriverExecutionUnit;
import org.apache.shardingsphere.infra.spi.typed.TypedSPI;

import java.sql.SQLException;

/**
 * SQL execution unit builder.
 *
 * @param <T> type of storage resource execute unit
 * @param <M> type of driver executor manager
 * @param <C> type of resource connection
 * @param <O> type of storage resource option
 */
public interface SQLExecutionUnitBuilder<T extends DriverExecutionUnit<?>, M extends ExecutorDriverManager<C, ?, O>, C, O extends StorageResourceOption> extends TypedSPI {
    
    /**
     * Build SQL execution unit.
     * 
     * @param executionUnit execution unit
     * @param executorManager executor manager 
     * @param connection connection
     * @param connectionMode connection mode
     * @param option storage resource option
     * @return SQL execution unit
     * @throws SQLException SQL exception
     */
    T build(ExecutionUnit executionUnit, M executorManager, C connection, ConnectionMode connectionMode, O option) throws SQLException;
}
