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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.StatementMemoryStrictlyFetchSizeSetter;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Statement memory strictly fetch size setter for PostgreSQL.
 */
@Getter
@Setter
public final class PostgreSQLStatementMemoryStrictlyFetchSizeSetter implements StatementMemoryStrictlyFetchSizeSetter {
    
    private Properties props;
    
    @Override
    public void setFetchSize(final Statement statement) throws SQLException {
        statement.setFetchSize(1);
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
