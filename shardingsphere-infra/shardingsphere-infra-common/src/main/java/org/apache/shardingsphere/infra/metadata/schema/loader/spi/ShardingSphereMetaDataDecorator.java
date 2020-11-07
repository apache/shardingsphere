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

package org.apache.shardingsphere.infra.metadata.schema.loader.spi;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPI;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalTableMetaData;

/**
 * ShardingSphere meta data decorator.
 */
public interface ShardingSphereMetaDataDecorator<T extends ShardingSphereRule> extends OrderedSPI<T> {
    
    /**
     * Decorate table meta data.
     *
     * @param tableName table name
     * @param tableMetaData table meta data
     * @param rule rule
     * @return decorated table meta data
     */
    PhysicalTableMetaData decorate(String tableName, PhysicalTableMetaData tableMetaData, T rule);
}
