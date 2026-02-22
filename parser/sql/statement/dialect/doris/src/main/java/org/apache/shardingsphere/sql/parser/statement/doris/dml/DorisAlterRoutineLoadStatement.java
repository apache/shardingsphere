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

package org.apache.shardingsphere.sql.parser.statement.doris.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.job.JobNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;

import java.util.Optional;

/**
 * Doris alter routine load statement.
 */
@Getter
@Setter
public final class DorisAlterRoutineLoadStatement extends DMLStatement {
    
    private JobNameSegment jobName;
    
    private PropertiesSegment jobProperties;
    
    private String dataSource;
    
    private PropertiesSegment dataSourceProperties;
    
    public DorisAlterRoutineLoadStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    public Optional<JobNameSegment> getJobName() {
        return Optional.ofNullable(jobName);
    }
    
    public Optional<PropertiesSegment> getJobProperties() {
        return Optional.ofNullable(jobProperties);
    }
    
    public Optional<String> getDataSource() {
        return Optional.ofNullable(dataSource);
    }
    
    public Optional<PropertiesSegment> getDataSourceProperties() {
        return Optional.ofNullable(dataSourceProperties);
    }
}
