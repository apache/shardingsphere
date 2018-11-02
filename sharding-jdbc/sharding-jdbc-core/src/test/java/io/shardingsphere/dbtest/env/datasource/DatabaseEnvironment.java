/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.dbtest.env.datasource;

import io.shardingsphere.core.constant.DatabaseType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Database environment.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class DatabaseEnvironment {
    
    private final DatabaseType databaseType;
    
    private final String host;
    
    private final int port;
    
    @Getter
    private final String username;
    
    @Getter
    private final String password;
    
    /**
     * Get driver class name.
     * 
     * @return driver class name
     */
    public String getDriverClassName() {
        switch (databaseType) {
            case H2:
                return "org.h2.Driver";
            case MySQL:
                return "com.mysql.jdbc.Driver";
            case PostgreSQL:
                return "org.postgresql.Driver";
            case SQLServer:
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case Oracle:
                return "oracle.jdbc.driver.OracleDriver";
            default:
                throw new UnsupportedOperationException(databaseType.name());
        }
    }
    
    /**
     * Get URL.
     *
     * @return URL
     */
    public String getURL() {
        switch (databaseType) {
            case H2:
                return "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
            case MySQL:
                return String.format("jdbc:mysql://%s:%s?serverTimezone=UTC&useSSL=false&useLocalSessionState=true", host, port);
            case PostgreSQL:
                return String.format("jdbc:postgresql://%s:%s/", host, port);
            case SQLServer:
                return String.format("jdbc:sqlserver://%s:%s", host, port);
            case Oracle:
                return String.format("jdbc:oracle:thin:@%s:%s", host, port);
            default:
                throw new UnsupportedOperationException(databaseType.name());
        }
    }
    
    /**
     * Get URL.
     *
     * @param dataSourceName data source name
     * @return URL
     */
    public String getURL(final String dataSourceName) {
        switch (databaseType) {
            case H2:
                return String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", dataSourceName);
            case MySQL:
                return String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC&useSSL=false&useLocalSessionState=true", host, port, dataSourceName);
            case PostgreSQL:
                return String.format("jdbc:postgresql://%s:%s/%s", host, port, dataSourceName);
            case SQLServer:
                return String.format("jdbc:sqlserver://%s:%s;DatabaseName=%s", host, port, dataSourceName);
            case Oracle:
                return String.format("jdbc:oracle:thin:@%s:%s:%s", host, port, dataSourceName);
            default:
                throw new UnsupportedOperationException(databaseType.name());
        }
    }
}
