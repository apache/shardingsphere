/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.integrate;

import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;

public final class ShardingJdbcDatabaseTester extends JdbcDatabaseTester {
    
    private String driverClass;
    
    public ShardingJdbcDatabaseTester(final String driverClass, final String connectionUrl, final String username,
            final String password) throws ClassNotFoundException {
        super(driverClass, connectionUrl, username, password, null);
        this.driverClass = driverClass;
    }
    
    @Override
    public IDatabaseConnection getConnection() throws Exception {
        IDatabaseConnection result = super.getConnection();
        if (org.h2.Driver.class.getName().equals(driverClass)) {
            result.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
        } else if (com.mysql.jdbc.Driver.class.getName().equals(driverClass)) {
            result.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
        }
        return result;
    }
}
