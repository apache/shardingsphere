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

package org.apache.shardingsphere.data.pipeline.api.metadata;

import lombok.Getter;
import lombok.NonNull;

import java.util.Objects;

/**
 * Table name.
 * <p>It might be logic table name or actual table name.</p>
 * <p>It's case-insensitive.</p>
 */
@Getter
public class TableName {
    
    @NonNull
    private final String original;
    
    @NonNull
    private final String lowercase;
    
    public TableName(final String tableName) {
        this.original = tableName;
        this.lowercase = tableName.toLowerCase();
    }
    
    // TODO table name case-sensitive for some database
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TableName tableName = (TableName) o;
        return lowercase.equals(tableName.lowercase);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(lowercase);
    }
    
    @Override
    public String toString() {
        return original;
    }
}
