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

package org.apache.shardingsphere.traffic.executor.context.builder;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.spi.typed.TypedSPI;
import org.apache.shardingsphere.traffic.executor.context.TrafficExecutorContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Traffic executor context builder.
 */
public interface TrafficExecutorContextBuilder<T extends Statement> extends TypedSPI {
    
    /**
     * Build traffic executor context.
     * 
     * @param logicSQL logic SQL
     * @param connection connection
     * @return traffic executor context
     * @throws SQLException SQL exception
     */
    TrafficExecutorContext<T> build(LogicSQL logicSQL, Connection connection) throws SQLException;
}
