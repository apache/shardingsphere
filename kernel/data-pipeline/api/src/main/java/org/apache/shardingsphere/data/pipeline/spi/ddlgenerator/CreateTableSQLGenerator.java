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

package org.apache.shardingsphere.data.pipeline.spi.ddlgenerator;

import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Create table SQL generator.
 */
@SingletonSPI
public interface CreateTableSQLGenerator extends DatabaseTypedSPI {
    
    /**
    * Generate create table SQLs.
    * 
    * @param dataSource dataSource
    * @param schemaName schema name
    * @param tableName table name
    * @return generated SQLs
    * @throws SQLException SQL exception
    */
    Collection<String> generate(DataSource dataSource, String schemaName, String tableName) throws SQLException;
}
