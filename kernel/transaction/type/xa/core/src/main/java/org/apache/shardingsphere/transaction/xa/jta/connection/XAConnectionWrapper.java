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

package org.apache.shardingsphere.transaction.xa.jta.connection;

import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * XA connection wrapper.
 */
@SingletonSPI
public interface XAConnectionWrapper extends DatabaseTypedSPI {
    
    /**
     * Wrap a normal connection to XA connection.
     *
     * @param xaDataSource XA data source
     * @param connection connection
     * @return sharding XA connection
     * @throws SQLException SQL exception
     */
    XAConnection wrap(XADataSource xaDataSource, Connection connection) throws SQLException;
}
