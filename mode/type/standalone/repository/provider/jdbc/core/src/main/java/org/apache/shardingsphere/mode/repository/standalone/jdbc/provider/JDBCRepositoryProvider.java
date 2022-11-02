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

package org.apache.shardingsphere.mode.repository.standalone.jdbc.provider;

import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

/**
 * JDBC repository provider.
 */
@SingletonSPI
public interface JDBCRepositoryProvider extends TypedSPI, RequiredSPI {
    
    /**
     * Drop table SQL.
     *
     * @return SQL to drop table
     */
    String dropTableSQL();
    
    /**
     * Create table SQL.
     *
     * @return SQL to create table
     */
    String createTableSQL();
    
    /**
     * Select by key SQL.
     *
     * @return SQL to select table
     */
    String selectByKeySQL();
    
    /**
     * Select by parent key SQL.
     *
     * @return SQL to select table
     */
    String selectByParentKeySQL();
    
    /**
     * Insert SQL.
     *
     * @return SQL to insert table
     */
    String insertSQL();
    
    /**
     * Update SQL.
     *
     * @return SQL to update table
     */
    String updateSQL();
    
    /**
     * Delete SQL.
     *
     * @return SQL to delete table
     */
    String deleteSQL();
}
