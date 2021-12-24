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

package org.apache.shardingsphere.scaling.postgresql.component.checker;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataCalculateParameter;
import org.apache.shardingsphere.data.pipeline.core.spi.check.consistency.AbstractSingleTableDataCalculator;
import org.apache.shardingsphere.data.pipeline.core.spi.check.consistency.CRC32MatchDataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;

import java.util.Collection;
import java.util.Collections;

/**
 * CRC32 match PostgreSQL implementation of single table data calculator.
 */
public final class CRC32MatchPostgreSQLSingleTableDataCalculator extends AbstractSingleTableDataCalculator {
    
    private static final Collection<String> DATABASE_TYPES = Collections.singletonList(new PostgreSQLDatabaseType().getName());
    
    @Override
    public String getAlgorithmType() {
        return CRC32MatchDataConsistencyCheckAlgorithm.TYPE;
    }
    
    @Override
    public Collection<String> getDatabaseTypes() {
        return DATABASE_TYPES;
    }
    
    @Override
    public Iterable<Object> calculate(final DataCalculateParameter dataCalculateParameter) {
        //TODO PostgreSQL calculate
        return Collections.emptyList();
    }
}
