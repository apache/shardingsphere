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

package io.shardingsphere.transaction.xa.fixture;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.PoolType;
import lombok.NoArgsConstructor;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import javax.sql.DataSource;

/**
 * Data source utility.
 *
 * @author zhaojun
 */
@NoArgsConstructor
public final class DataSourceUtils {
    
    /**
     * Build data source.
     *
     * @param poolType pool type
     * @param databaseType database type
     * @return data source
     */
    public static DataSource build(final PoolType poolType, final DatabaseType databaseType) {
        switch (poolType) {
            case DBCP2:
                return newBasicDataSource(databaseType);
            case DBCP2_TOMCAT:
                return newTomcatBasicDataSource(databaseType);
            case HIKARI:
                return newHikariDataSource(databaseType);
            case DRUID:
                return newDruidDataSource(databaseType);
            default:
                return newHikariDataSource(databaseType);
        }
    }
    
    private static org.apache.commons.dbcp2.BasicDataSource newBasicDataSource(final DatabaseType databaseType) {
        org.apache.commons.dbcp2.BasicDataSource result = new org.apache.commons.dbcp2.BasicDataSource();
        result.setUrl(getUrl(databaseType));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaxTotal(10);
        result.setMaxWaitMillis(2000);
        result.setMaxIdle(200);
        result.setMaxConnLifetimeMillis(100000);
        return result;
    }
    
    private static BasicDataSource newTomcatBasicDataSource(final DatabaseType databaseType) {
        BasicDataSource result = new BasicDataSource();
        result.setUrl(getUrl(databaseType));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaxTotal(10);
        result.setMaxWaitMillis(2000);
        result.setMaxIdle(200);
        result.setMaxConnLifetimeMillis(100000);
        return result;
    }
    
    private static DruidDataSource newDruidDataSource(final DatabaseType databaseType) {
        DruidDataSource result = new DruidDataSource();
        result.setUrl(getUrl(databaseType));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaxActive(10);
        result.setMaxWait(2000);
        result.setMaxIdle(200);
        result.setMinEvictableIdleTimeMillis(100000);
        return result;
    }
    
    private static HikariDataSource newHikariDataSource(final DatabaseType databaseType) {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl(getUrl(databaseType));
        result.setUsername("root");
        result.setPassword("root");
        result.setMaximumPoolSize(10);
        result.setConnectionTimeout(2000);
        result.setIdleTimeout(200);
        result.setMaxLifetime(100000);
        return result;
    }
    
    private static String getUrl(final DatabaseType databaseType) {
        switch (databaseType) {
            case PostgreSQL:
                return "jdbc:postgresql://localhost:3306/demo_ds";
            case MySQL:
                return "jdbc:mysql://localhost:3306/demo_ds";
            case SQLServer:
                return "jdbc:sqlserver://localhost:1433;DatabaseName=demo_ds";
            case H2:
                return "jdbc:h2:mem:db0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL";
            case Oracle:
                return "jdbc:oracle:thin:@//localhost:3306/demo_ds";
            default:
                return "jdbc:mysql://localhost:3306/demo_ds";
        }
    }
}
