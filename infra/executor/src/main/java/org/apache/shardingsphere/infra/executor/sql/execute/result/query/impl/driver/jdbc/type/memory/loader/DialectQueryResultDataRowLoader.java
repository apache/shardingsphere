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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.loader;

import org.apache.shardingsphere.infra.executor.sql.execute.result.query.type.memory.row.MemoryQueryResultDataRow;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Dialect query result data row loader.
 */
@SingletonSPI
public interface DialectQueryResultDataRowLoader extends DatabaseTypedSPI {
    
    /**
     * Load rows.
     *
     * @param columnCount column count
     * @param resultSet result set of JDBC
     * @return query result data rows
     * @throws SQLException SQL exception
     */
    Collection<MemoryQueryResultDataRow> load(int columnCount, ResultSet resultSet) throws SQLException;
}
