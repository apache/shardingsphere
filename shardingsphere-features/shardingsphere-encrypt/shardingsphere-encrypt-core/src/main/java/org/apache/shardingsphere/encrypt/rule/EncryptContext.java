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

package org.apache.shardingsphere.encrypt.rule;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.Setter;

/**
 * Encrypt context.
 */
@Getter
@Setter
public final class EncryptContext {
    
    private final String schema;
    
    private final String owner;
    
    private final String table;
    
    private final String column;
    
    public EncryptContext(final String schema, final String owner, final String table, final String column) {
        this.schema = schema;
        this.owner = owner;
        this.table = table;
        this.column = column;
    }
    
    /**
     * Encrypt context map.
     * @return encrypt context map which includes schema, owner, table and column keys.
     */
    public Map<String, String> of() {
        return ImmutableMap.of("schema", schema, "owner", owner, "table", table, "column", column);
    }
}
