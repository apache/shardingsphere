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

package org.apache.shardingsphere.test.it.rewriter.engine.mocker;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Dialect storage unit meta data mocker.
 */
public interface DialectStorageUnitMetaDataMocker extends DatabaseTypedSPI {
    
    /**
     * Mock storage unit meta data.
     *
     * @param connection connection
     * @param databaseMetaData database meta data
     */
    void mockStorageUnitMetaData(Connection connection, DatabaseMetaData databaseMetaData);
}
