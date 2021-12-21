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

package org.apache.shardingsphere.data.pipeline.core.check.consistency;

import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.SingleTableDataCalculator;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;

import java.util.Collection;
import java.util.Collections;

/**
 * Scaling default data consistency check algorithm.
 */
public final class DefaultDataConsistencyCheckAlgorithm implements DataConsistencyCheckAlgorithm {
    
    public static final String TYPE = "DEFAULT";
    
    private static final Collection<String> SUPPORTED_DATABASE_TYPES = Collections.singletonList(new MySQLDatabaseType().getName());
    
    @Override
    public void init() {
    }
    
    @Override
    public String getDescription() {
        return "Default implementation with CRC32 of all records.";
    }
    
    @Override
    public Collection<String> getSupportedDatabaseTypes() {
        return SUPPORTED_DATABASE_TYPES;
    }
    
    @Override
    public String getProvider() {
        return "ShardingSphere";
    }
    
    @Override
    public SingleTableDataCalculator getSingleTableDataCalculator(final String supportedDatabaseType) {
        return SingleTableDataCalculatorRegistry.newServiceInstance(TYPE, supportedDatabaseType);
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}
