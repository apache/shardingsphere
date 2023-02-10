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

package org.apache.shardingsphere.single.metadata.reviser;

import org.apache.shardingsphere.infra.metadata.database.schema.reviser.MetaDataReviseEntry;
import org.apache.shardingsphere.single.constant.SingleOrder;
import org.apache.shardingsphere.single.metadata.reviser.constraint.SingleConstraintReviser;
import org.apache.shardingsphere.single.metadata.reviser.index.SingleIndexReviser;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.Optional;

/**
 * Single meta data revise entry.
 */
public final class SingleMetaDataReviseEntry implements MetaDataReviseEntry<SingleRule> {
    
    @Override
    public Optional<SingleIndexReviser> getIndexReviser(final SingleRule rule, final String tableName) {
        return Optional.of(new SingleIndexReviser());
    }
    
    @Override
    public Optional<SingleConstraintReviser> getConstraintReviser(final SingleRule rule, final String tableName) {
        return Optional.of(new SingleConstraintReviser());
    }
    
    @Override
    public int getOrder() {
        return SingleOrder.ORDER;
    }
    
    @Override
    public Class<SingleRule> getTypeClass() {
        return SingleRule.class;
    }
}
