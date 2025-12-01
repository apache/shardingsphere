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

package org.apache.shardingsphere.distsql.handler.executor.rql.resource;

import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.util.Collection;

/**
 * In used storage unit retriever.
 * 
 * @param <T> type of rule
 */
@SingletonSPI
public interface InUsedStorageUnitRetriever<T extends ShardingSphereRule> extends TypedSPI {
    
    /**
     * Get in used resources.
     *
     * @param sqlStatement show rules used storage unit statement
     * @param rule rule
     * @return in used resources
     */
    Collection<String> getInUsedResources(ShowRulesUsedStorageUnitStatement sqlStatement, T rule);
    
    @Override
    Class<T> getType();
}
