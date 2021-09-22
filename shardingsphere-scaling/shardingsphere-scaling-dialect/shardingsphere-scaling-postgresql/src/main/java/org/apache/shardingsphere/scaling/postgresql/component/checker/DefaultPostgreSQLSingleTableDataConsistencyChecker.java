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

import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.scaling.core.api.impl.AbstractSingleTableDataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.api.impl.ScalingDefaultDataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.scaling.core.job.JobContext;

import java.util.Collection;

/**
 * Default PostgreSQL single table data consistency checker.
 */
public final class DefaultPostgreSQLSingleTableDataConsistencyChecker extends AbstractSingleTableDataConsistencyChecker {
    
    private static final String DATABASE_TYPE = new PostgreSQLDatabaseType().getName();
    
    public DefaultPostgreSQLSingleTableDataConsistencyChecker(final JobContext jobContext) {
        super(jobContext);
    }
    
    @Override
    public String getAlgorithmType() {
        return ScalingDefaultDataConsistencyCheckAlgorithm.TYPE;
    }
    
    @Override
    public String getDatabaseType() {
        return DATABASE_TYPE;
    }
    
    @Override
    public boolean dataCheck(final String logicTableName, final Collection<String> columnNames) {
        //TODO PostgreSQL dataCheck
        return true;
    }
}
