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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Raw query result column meta data.
 */
@RequiredArgsConstructor
@Getter
public final class RawQueryResultColumnMetaData {
    
    private final String tableName;
    
    private final String name;
    
    private final String label;
    
    private final int type;
    
    private final String typeName;
    
    private final int length;
    
    private final int decimals;
    
    private final boolean signed;
    
    private final boolean notNull;
    
    private final boolean autoIncrement;
    
    public RawQueryResultColumnMetaData(final String tableName, final String name, final String label, final int type, final String typeName, final int length, final int decimals) {
        this.tableName = tableName;
        this.name = name;
        this.label = label;
        this.type = type;
        this.typeName = typeName;
        this.length = length;
        this.decimals = decimals;
        signed = false;
        notNull = false;
        autoIncrement = false;
    }
}
