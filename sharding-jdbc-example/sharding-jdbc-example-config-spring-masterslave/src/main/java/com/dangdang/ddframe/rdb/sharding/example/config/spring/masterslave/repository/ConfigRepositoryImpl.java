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

package com.dangdang.ddframe.rdb.sharding.example.config.spring.masterslave.repository;

import com.dangdang.ddframe.rdb.sharding.spring.datasource.SpringShardingDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Repository
public class ConfigRepositoryImpl implements ConfigRepository {
    
    @Resource
    private SpringShardingDataSource shardingDataSource;
    
    @Override
    public void select() {
        String sql = "SELECT c.* FROM t_config c";
        try (
            Connection conn = shardingDataSource.getConnection();
            PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    System.out.println("configName:" + rs.getString(2) + ",configValue:" + rs.getString(3));
                }
            }
        // CHECKSTYLE:OFF
        } catch (final Exception ex) {
        // CHECKSTYLE:ON
            ex.printStackTrace();
        }
    }
}
