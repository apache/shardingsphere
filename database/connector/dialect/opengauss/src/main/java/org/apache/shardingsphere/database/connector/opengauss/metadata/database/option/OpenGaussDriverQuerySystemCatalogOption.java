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

package org.apache.shardingsphere.database.connector.opengauss.metadata.database.option;

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.table.DialectDriverQuerySystemCatalogOption;

import java.util.Arrays;
import java.util.Collection;

/**
 * Driver query system catalog option for openGauss.
 */
public final class OpenGaussDriverQuerySystemCatalogOption implements DialectDriverQuerySystemCatalogOption {
    
    private static final Collection<String> SYSTEM_CATALOG_QUERY_EXPRESSIONS = new CaseInsensitiveSet<>(
            Arrays.asList("version()", "intervaltonum(gs_password_deadline())", "gs_password_notifytime()"));
    
    private static final String DATABASE_DATA_TABLE = "pg_database";
    
    private static final String TABLE_DATA_TABLE = "pg_tables";
    
    private static final String ROLE_DATA_TABLE = "pg_roles";
    
    private static final Collection<String> SYSTEM_CATALOG_TABLES = new CaseInsensitiveSet<>(Arrays.asList(DATABASE_DATA_TABLE, TABLE_DATA_TABLE, ROLE_DATA_TABLE));
    
    @Override
    public boolean isSystemCatalogQueryExpressions(final String projectionExpression) {
        return SYSTEM_CATALOG_QUERY_EXPRESSIONS.contains(projectionExpression);
    }
    
    @Override
    public boolean isSystemTable(final String tableName) {
        return SYSTEM_CATALOG_TABLES.contains(tableName);
    }
    
    @Override
    public boolean isDatabaseDataTable(final String tableName) {
        return DATABASE_DATA_TABLE.equalsIgnoreCase(tableName);
    }
    
    @Override
    public boolean isTableDataTable(final String tableName) {
        return TABLE_DATA_TABLE.equalsIgnoreCase(tableName);
    }
    
    @Override
    public boolean isRoleDataTable(final String tableName) {
        return ROLE_DATA_TABLE.equalsIgnoreCase(tableName);
    }
    
    @Override
    public String getDatCompatibility() {
        return "PG";
    }
}
