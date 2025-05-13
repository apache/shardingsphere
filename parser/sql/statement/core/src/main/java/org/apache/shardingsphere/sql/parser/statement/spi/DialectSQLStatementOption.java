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

package org.apache.shardingsphere.sql.parser.statement.spi;

import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

/**
 * Dialect SQL statement option.
 */
@SingletonSPI
public interface DialectSQLStatementOption extends DatabaseTypedSPI {
    
    /**
     * Whether show columns SQL statement.
     * 
     * @param sqlStatement SQL statement
     * @return is show columns SQL statement or not
     */
    boolean isShowColumns(SQLStatement sqlStatement);
    
    /**
     * Whether show create table SQL statement.
     *
     * @param sqlStatement SQL statement
     * @return is show create table SQL statement or not
     */
    boolean isShowCreateTable(SQLStatement sqlStatement);
}
