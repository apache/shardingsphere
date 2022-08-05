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

package org.apache.shardingsphere.mode.repository.standalone.h2;

import org.apache.shardingsphere.mode.repository.standalone.jdbc.provider.JDBCRepositoryProvider;

/**
 * H2 JDBC repository provider.
 */
public final class H2JDBCRepositoryProvider implements JDBCRepositoryProvider {
    
    @Override
    public String dropTableSQL() {
        return "DROP TABLE IF EXISTS `repository`";
    }
    
    @Override
    public String createTableSQL() {
        return "CREATE TABLE `repository`(id varchar(36) PRIMARY KEY, `key` TEXT, `value` TEXT, parent TEXT)";
    }
    
    @Override
    public String selectByKeySQL() {
        return "SELECT `value` FROM `repository` WHERE `key` = ?";
    }
    
    @Override
    public String selectByParentKeySQL() {
        return "SELECT `key` FROM `repository` WHERE parent = ?";
    }
    
    @Override
    public String insertSQL() {
        return "INSERT INTO `repository` VALUES(?, ?, ?, ?)";
    }
    
    @Override
    public String updateSQL() {
        return "UPDATE `repository` SET `value` = ? WHERE `key` = ?";
    }
    
    @Override
    public String deleteSQL() {
        return "DELETE FROM `repository` WHERE `key` = ?";
    }
    
    @Override
    public String getType() {
        return "H2";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
