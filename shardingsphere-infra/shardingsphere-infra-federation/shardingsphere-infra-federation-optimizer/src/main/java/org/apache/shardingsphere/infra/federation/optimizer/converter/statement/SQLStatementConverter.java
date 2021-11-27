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

package org.apache.shardingsphere.infra.federation.optimizer.converter.statement;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

/**
 * SQL Statement converter.
 * 
 * @param <T> type of SQL statement
 */
public interface SQLStatementConverter<S extends SQLStatement, T extends SqlNode> {
    
    /**
     * Convert SQL statement to SQL node.
     * 
     * @param sqlStatement SQL statement be to converted
     * @return converted SQL node
     */
    T convertToSQLNode(S sqlStatement);
    
    /**
     * Convert SQL node to SQL statement.
     *
     * @param sqlNode SQL node be to converted
     * @return converted SQL statement
     */
    S convertToSQLStatement(T sqlNode);
}
