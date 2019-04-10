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

package org.apache.shardingsphere.example.broadcast.table.raw.jdbc;

import org.apache.shardingsphere.example.broadcast.table.raw.jdbc.factory.YamlDataSourceFactory;
import org.apache.shardingsphere.example.common.jdbc.repository.CountryRepositroyImpl;
import org.apache.shardingsphere.example.common.jdbc.service.CountryServiceImpl;
import org.apache.shardingsphere.example.common.service.CommonService;
import org.apache.shardingsphere.example.type.ShardingType;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

/*
 * Please make sure master-slave data sync on MySQL is running correctly. Otherwise this example will query empty data from slave.
 */
public class YamlConfigurationExample {

    private static ShardingType shardingType = ShardingType.SHARDING_DATABASES;
//    private static ShardingType shardingType = ShardingType.MASTER_SLAVE;

    public static void main(final String[] args) throws SQLException, IOException {
        DataSource dataSource = YamlDataSourceFactory.newInstance(shardingType);
        CommonService countryService = getCountryService(dataSource);
        countryService.initEnvironment();
        countryService.processSuccess();
        countryService.cleanEnvironment();
    }

    private static CommonService getCountryService(final DataSource dataSource) {
        return new CountryServiceImpl(new CountryRepositroyImpl(dataSource));
    }
}
