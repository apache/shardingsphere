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

package org.apache.shardingsphere.distsql.handler.type.rdl.resource.aware;

import org.apache.shardingsphere.distsql.handler.type.rdl.resource.ResourceDefinitionExecutor;
import org.apache.shardingsphere.distsql.statement.rdl.resource.ResourceDefinitionStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

/**
 * Database aware resource definition executor.
 * 
 * @param <T> type of resource definition statement
 */
public interface DatabaseAwareResourceDefinitionExecutor<T extends ResourceDefinitionStatement> extends ResourceDefinitionExecutor<T> {
    
    /**
     * Set database.
     *
     * @param database database
     */
    void setDatabase(ShardingSphereDatabase database);
}
