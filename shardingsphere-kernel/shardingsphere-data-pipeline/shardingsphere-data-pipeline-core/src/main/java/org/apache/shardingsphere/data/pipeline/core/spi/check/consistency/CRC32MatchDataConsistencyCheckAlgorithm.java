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

package org.apache.shardingsphere.data.pipeline.core.spi.check.consistency;

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;

import java.util.Collection;
import java.util.Collections;

/**
 * CRC32 match implementation of data consistency check algorithm.
 */
public final class CRC32MatchDataConsistencyCheckAlgorithm extends AbstractDataConsistencyCheckAlgorithm {
    
    public static final String TYPE = "CRC32_MATCH";
    
    private static final Collection<String> SUPPORTED_DATABASE_TYPES = Collections.singletonList(new MySQLDatabaseType().getName());
    
    @Override
    public String getDescription() {
        return "Match CRC32 of records.";
    }
    
    @Override
    public Collection<String> getSupportedDatabaseTypes() {
        return SUPPORTED_DATABASE_TYPES;
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
