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

package org.apache.shardingsphere.proxy.backend.state;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

import java.util.Collection;

/**
 * Dialect unavailable proxy state supported SQL provider.
 */
@SingletonSPI
public interface DialectProxyStateSupportedSQLProvider extends DatabaseTypedSPI {
    
    /**
     * Get supported SQL statement types on ready proxy state.
     *
     * @return unsupported SQL statement types
     */
    Collection<Class<? extends SQLStatement>> getUnsupportedSQLStatementTypesOnReadyState();
    
    /**
     * Get supported SQL statement types on unavailable proxy state.
     *
     * @return supported SQL statement types
     */
    Collection<Class<? extends SQLStatement>> getSupportedSQLStatementTypesOnUnavailableState();
}
