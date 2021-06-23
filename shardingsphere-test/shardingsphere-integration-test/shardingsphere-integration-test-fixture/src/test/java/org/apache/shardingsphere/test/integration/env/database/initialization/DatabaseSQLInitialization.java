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

package org.apache.shardingsphere.test.integration.env.database.initialization;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.typed.TypedSPI;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Database SQL initialization.
 */
public interface DatabaseSQLInitialization extends TypedSPI {
    
    /**
     * Execute init SQLs.
     *
     * @param scenario scenario
     * @param databaseType database type
     * @param dataSourceMap datasource map
     *
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     * @throws JAXBException JAXB exception
     */
    void executeInitSQLs(String scenario, DatabaseType databaseType, Map<String, DataSource> dataSourceMap) throws IOException, SQLException, JAXBException;
}
