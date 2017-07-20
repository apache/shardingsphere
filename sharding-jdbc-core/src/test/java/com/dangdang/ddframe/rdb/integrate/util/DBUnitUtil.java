/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.integrate.util;

import com.dangdang.ddframe.rdb.integrate.sql.DatabaseTestSQL;
import com.dangdang.ddframe.rdb.integrate.sql.mysql.MySQLTestSQL;
import com.dangdang.ddframe.rdb.integrate.sql.oracle.OracleSQLTestSQL;
import com.dangdang.ddframe.rdb.integrate.sql.postgresql.PostgreSQLTestSQL;
import com.dangdang.ddframe.rdb.integrate.sql.sqlserver.SQLServerSQLTestSQL;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.h2.H2Connection;
import org.dbunit.ext.mssql.MsSqlConnection;
import org.dbunit.ext.mysql.MySqlConnection;
import org.dbunit.ext.oracle.OracleConnection;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;

import java.sql.Connection;

public class DBUnitUtil {
    
    public static IDatabaseConnection getConnection(final DataBaseEnvironment dbEnv, final Connection connection) throws DatabaseUnitException {
        switch (dbEnv.getDatabaseType()) {
            case H2:
                return new H2Connection(connection, "PUBLIC");
            case MySQL:
                return new MySqlConnection(connection, null);
            case PostgreSQL:
                DatabaseConnection databaseConnection = new DatabaseConnection(connection);
                databaseConnection.getConfig().setProperty("http://www.dbunit.org/properties/datatypeFactory", new PostgresqlDataTypeFactory());
                return databaseConnection;
            case Oracle:
                return new OracleConnection(connection, "JDBC");
            case SQLServer:
                return new MsSqlConnection(connection);
            default:
                throw new UnsupportedOperationException(dbEnv.getDatabaseType().name());
        }
    }
    
    public static DatabaseTestSQL currentDatabaseTestSQL(final DataBaseEnvironment dbEnv) {
        switch (dbEnv.getDatabaseType()) {
            case H2:
            case MySQL:
                return new MySQLTestSQL();
            case PostgreSQL:
                return new PostgreSQLTestSQL();
            case Oracle:
                return new OracleSQLTestSQL();
            case SQLServer:
                return new SQLServerSQLTestSQL();
            default:
                throw new UnsupportedOperationException(dbEnv.getDatabaseType().name());
        }
    }
    
}
