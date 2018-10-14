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

package io.shardingsphere.example.proxy.main;

import io.shardingsphere.example.repository.jdbc.repository.RawOrderItemRepository;
import io.shardingsphere.example.repository.jdbc.repository.RawOrderRepository;
import io.shardingsphere.example.repository.jdbc.service.RawDemoService;

import javax.sql.DataSource;

/*
 * 1. Copy resources/config.yaml to sharding-proxy conf folder and overwrite original file.
 * 2. Please make sure sharding-proxy is running before you run this example.
 */
public final class ProxyMain {
    
    private static final String PROXY_IP = "localhost";
    
    private static final int PROXY_PORT = 3307;
    
    public static void main(final String[] args) {
        DataSource dataSource = DataSourceUtil.createDataSource(PROXY_IP, PROXY_PORT);
        new RawDemoService(new RawOrderRepository(dataSource), new RawOrderItemRepository(dataSource)).demo();
    }
}
