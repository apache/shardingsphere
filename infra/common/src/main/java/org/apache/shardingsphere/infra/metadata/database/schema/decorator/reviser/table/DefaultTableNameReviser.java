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

package org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.table;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Optional;

/**
 * Default table name reviser.
 */
public final class DefaultTableNameReviser implements TableNameReviser<ShardingSphereRule, Object> {
    
    @Override
    public String revise(final String originalName, final Object tableRule) {
        return originalName;
    }
    
    @Override
    public Optional<Object> findTableRule(final String name, final ShardingSphereRule rule) {
        return Optional.empty();
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
