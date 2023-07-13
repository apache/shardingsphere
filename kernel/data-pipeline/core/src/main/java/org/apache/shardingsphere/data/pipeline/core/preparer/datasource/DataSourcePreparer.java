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

package org.apache.shardingsphere.data.pipeline.core.preparer.datasource;

import org.apache.shardingsphere.infra.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;

import java.sql.SQLException;

/**
 * Data source preparer.
 */
@SingletonSPI
public interface DataSourcePreparer extends DatabaseTypedSPI {
    
    /**
     * Prepare target schemas.
     *
     * @param param prepare target schemas parameter
     * @throws SQLException if prepare target schema fail
     */
    void prepareTargetSchemas(PrepareTargetSchemasParameter param) throws SQLException;
    
    /**
     * Prepare target tables.
     *
     * @param param prepare target tables parameter
     * @throws SQLException SQL exception
     */
    void prepareTargetTables(PrepareTargetTablesParameter param) throws SQLException;
}
