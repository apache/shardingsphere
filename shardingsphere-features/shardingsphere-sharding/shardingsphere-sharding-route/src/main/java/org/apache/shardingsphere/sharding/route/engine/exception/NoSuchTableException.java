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

package org.apache.shardingsphere.sharding.route.engine.exception;

import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

/**
 * No such table exception.
 */
@Getter
public final class NoSuchTableException extends ShardingSphereException {
    
    private static final long serialVersionUID = 8311953084941769743L;
    
    private final String databaseName;
    
    private final String tableName;
    
    public NoSuchTableException(final String databaseName, final String tableName) {
        super(String.format("Table '%s.%s' doesn't exist", databaseName, tableName));
        this.databaseName = databaseName;
        this.tableName = tableName;
    }
}
