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

package org.apache.shardingsphere.proxy.backend.communication;

import java.sql.SQLException;

/**
 * Transaction manager interface for proxy.
 *
 * @param <T> return type of methods
 */
public interface TransactionManager<T> {
    
    /**
     * Begin transaction.
     *
     * @return can be Void or Future
     * @throws SQLException SQL exception
     */
    T begin() throws SQLException;
    
    /**
     * Commit transaction.
     *
     * @return can be Void or Future
     * @throws SQLException SQL exception
     */
    T commit() throws SQLException;
    
    /**
     * Rollback transaction.
     *
     * @return can be Void or Future
     * @throws SQLException SQL exception
     */
    T rollback() throws SQLException;
    
    /**
     * Set savepoint.
     *
     * @param savepointName savepoint name
     * @return can be Void or Future
     * @throws SQLException SQL exception
     */
    T setSavepoint(String savepointName) throws SQLException;
    
    /**
     * Rollback to savepoint.
     *
     * @param savepointName savepoint name
     * @return can be Void or Future
     * @throws SQLException SQL exception
     */
    T rollbackTo(String savepointName) throws SQLException;
    
    /**
     * Release savepoint.
     *
     * @param savepointName savepoint name
     * @return can be Void or Future
     * @throws SQLException SQL exception
     */
    T releaseSavepoint(String savepointName) throws SQLException;
}
