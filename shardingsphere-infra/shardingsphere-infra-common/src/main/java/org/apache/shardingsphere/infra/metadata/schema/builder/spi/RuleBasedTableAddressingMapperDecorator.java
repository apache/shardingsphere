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

package org.apache.shardingsphere.infra.metadata.schema.builder.spi;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPI;

import java.util.Collection;
import java.util.Map;

/**
 * Table addressing mapper decorator with related rule.
 * 
 * @param <T> type of ShardingSphere rule
 */
public interface RuleBasedTableAddressingMapperDecorator<T extends ShardingSphereRule> extends OrderedSPI<T> {
    
    /**
     * Decorate table addressing mapper with data source names.
     *
     * @param rule ShardingSphere rule
     * @param tableAddressingMapper decorated table addressing mapper
     */
    void decorate(T rule, Map<String, Collection<String>> tableAddressingMapper);
}
