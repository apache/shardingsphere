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

package org.apache.shardingsphere.database.connector.doris.metadata.identifier;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.DialectIdentifierCasePolicyLoader;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicySet;
import org.apache.shardingsphere.database.connector.mysql.metadata.identifier.MySQLIdentifierCasePolicyLoader;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Doris loader of identifier case policies.
 */
public final class DorisIdentifierCasePolicyLoader implements DialectIdentifierCasePolicyLoader {
    
    private final MySQLIdentifierCasePolicyLoader delegate = new MySQLIdentifierCasePolicyLoader();
    
    @Override
    public IdentifierCasePolicySet load(final Connection connection) throws SQLException {
        return delegate.load(connection);
    }
    
    @Override
    public String getDatabaseType() {
        return "Doris";
    }
}
