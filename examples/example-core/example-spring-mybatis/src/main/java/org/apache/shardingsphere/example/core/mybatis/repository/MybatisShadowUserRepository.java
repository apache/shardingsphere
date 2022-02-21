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

package org.apache.shardingsphere.example.core.mybatis.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.shardingsphere.example.core.api.entity.ShadowUser;
import org.apache.shardingsphere.example.core.api.repository.ShadowUserRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface MybatisShadowUserRepository extends ShadowUserRepository {
    
    @Override
    default void createTableIfNotExists() throws SQLException {
        createTableIfNotExistsNative();
        createTableIfNotExistsShadow();
    }
    
    void createTableIfNotExistsNative();
    
    void createTableIfNotExistsShadow();
    
    @Override
    default void truncateTable() throws SQLException {
        truncateTableShadow();
        truncateTableNative();
    }
    
    void truncateTableNative();
    
    void truncateTableShadow();
    
    @Override
    default void dropTable() throws SQLException {
        dropTableShadow();
        dropTableNative();
    }
    
    void dropTableNative();
    
    void dropTableShadow();
    
    @Override
    default List<ShadowUser> selectAll() throws SQLException {
        List<ShadowUser> result = new ArrayList<>();
        result.addAll(selectAllByShadow(0));
        result.addAll(selectAllByShadow(1));
        return result;
    }
    
    List<ShadowUser> selectAllByShadow(int userType) throws SQLException;
    
    @Override
    default void delete(Long primaryKey) throws SQLException {
        Map<String, Long> idTypeMapping = new HashMap<>(2);
        idTypeMapping.put("userId", primaryKey);
        idTypeMapping.put("userType", primaryKey % 2);
        deleteOne(idTypeMapping);
    }
    
    void deleteOne(Map<String, Long> idTypeMapping);
}
