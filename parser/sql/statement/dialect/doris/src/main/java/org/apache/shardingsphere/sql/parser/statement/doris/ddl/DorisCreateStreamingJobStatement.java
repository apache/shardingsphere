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

package org.apache.shardingsphere.sql.parser.statement.doris.ddl;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.job.JobCommentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.job.JobNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;

import java.util.Optional;

/**
 * Create streaming job statement for Doris.
 */
@Getter
@Setter
public final class DorisCreateStreamingJobStatement extends DDLStatement {
    
    private JobNameSegment jobName;
    
    private PropertiesSegment jobProperties;
    
    private JobCommentSegment comment;
    
    private String sourceType;
    
    private PropertiesSegment sourceProperties;
    
    private String targetDatabase;
    
    private PropertiesSegment targetProperties;
    
    private InsertStatement insertStatement;
    
    public DorisCreateStreamingJobStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Get job properties.
     *
     * @return job properties
     */
    public Optional<PropertiesSegment> getJobProperties() {
        return Optional.ofNullable(jobProperties);
    }
    
    /**
     * Get comment.
     *
     * @return comment
     */
    public Optional<JobCommentSegment> getComment() {
        return Optional.ofNullable(comment);
    }
    
    /**
     * Get source type.
     *
     * @return source type
     */
    public Optional<String> getSourceType() {
        return Optional.ofNullable(sourceType);
    }
    
    /**
     * Get source properties.
     *
     * @return source properties
     */
    public Optional<PropertiesSegment> getSourceProperties() {
        return Optional.ofNullable(sourceProperties);
    }
    
    /**
     * Get target database.
     *
     * @return target database
     */
    public Optional<String> getTargetDatabase() {
        return Optional.ofNullable(targetDatabase);
    }
    
    /**
     * Get target properties.
     *
     * @return target properties
     */
    public Optional<PropertiesSegment> getTargetProperties() {
        return Optional.ofNullable(targetProperties);
    }
}
