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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Statement replay callback.
 *
 * @param <T> type of statement
 */
public interface StatementReplayCallback<T extends Statement> {
    
    /**
     * Replay statement.
     *
     * @param statements statements
     * @param parameterSets parameter sets
     * @throws SQLException SQL exception
     */
    void replay(List<T> statements, List<List<Object>> parameterSets) throws SQLException;
}
