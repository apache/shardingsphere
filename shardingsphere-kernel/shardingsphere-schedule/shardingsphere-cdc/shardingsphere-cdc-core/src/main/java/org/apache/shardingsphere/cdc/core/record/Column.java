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

package org.apache.shardingsphere.cdc.core.record;

import lombok.Getter;
import lombok.Setter;

/**
 * Column.
 */
@Getter
public final class Column {
    
    private final String name;
    
    /**
     * Value are available only when the primary key column is updated.
     */
    @Setter
    private Object oldValue;
    
    private final Object value;
    
    private final boolean updated;
    
    private final boolean primaryKey;
    
    public Column(final String name, final Object value, final boolean updated, final boolean primaryKey) {
        this(name, null, value, updated, primaryKey);
    }
    
    public Column(final String name, final Object oldValue, final Object value, final boolean updated, final boolean primaryKey) {
        this.name = name;
        this.oldValue = oldValue;
        this.value = value;
        this.updated = updated;
        this.primaryKey = primaryKey;
    }
    
    @Override
    public String toString() {
        return String.format("%s=%s", name, value);
    }
}
