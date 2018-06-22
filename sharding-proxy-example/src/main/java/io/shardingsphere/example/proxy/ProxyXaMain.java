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

package io.shardingsphere.example.proxy;

import io.shardingsphere.example.proxy.repository.RawJdbcRepository;
import io.shardingsphere.example.proxy.repository.XaRawJdbcRepository;
import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

public class ProxyXaMain {
    private static final String PROXY_IP = "localhost";
    
    private static final int PROXY_PORT = 3307;
    
    public static void main(String[] args) throws SQLException {
        XaRawJdbcRepository rawJdbcRepository = new XaRawJdbcRepository(createDataSource());
        rawJdbcRepository.demo();
    }
    
    private static DataSource createDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://%s:%d/sharding_db?useServerPrepStmts=true&cachePrepStmts=true", PROXY_IP, PROXY_PORT));
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
}
