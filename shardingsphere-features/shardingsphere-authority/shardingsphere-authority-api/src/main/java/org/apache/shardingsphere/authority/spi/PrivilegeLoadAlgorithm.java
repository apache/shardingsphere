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

package org.apache.shardingsphere.authority.spi;

import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Privilege load algorithm.
 */
public interface PrivilegeLoadAlgorithm extends ShardingSphereAlgorithm {
    
    /**
     * Load privileges.
     * 
     * @param schemaName schema name
     * @param databaseType database type
     * @param dataSources data sources
     * @param rules rules
     * @param users users
     * @return user and privileges map
     */
    Map<ShardingSphereUser, ShardingSpherePrivilege> load(String schemaName, DatabaseType databaseType, 
                                                          Collection<DataSource> dataSources, Collection<ShardingSphereRule> rules, Collection<ShardingSphereUser> users);
}
