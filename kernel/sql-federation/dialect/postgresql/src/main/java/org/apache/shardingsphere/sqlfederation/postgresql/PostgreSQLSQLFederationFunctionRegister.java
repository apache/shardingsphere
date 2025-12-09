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

package org.apache.shardingsphere.sqlfederation.postgresql;

import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.SQLFederationFunctionRegister;
import org.apache.shardingsphere.sqlfederation.compiler.sql.function.postgresql.impl.PostgreSQLSystemFunction;

/**
 * SQL federation function register for PostgreSQL.
 */
public final class PostgreSQLSQLFederationFunctionRegister implements SQLFederationFunctionRegister {
    
    @Override
    public void registerFunction(final SchemaPlus schemaPlus, final String schemaName) {
        if ("pg_catalog".equalsIgnoreCase(schemaName)) {
            schemaPlus.add("pg_table_is_visible", ScalarFunctionImpl.create(PostgreSQLSystemFunction.class, "pgTableIsVisible"));
            schemaPlus.add("pg_get_userbyid", ScalarFunctionImpl.create(PostgreSQLSystemFunction.class, "pgGetUserById"));
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
