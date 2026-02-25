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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.job.BinlogDescriptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.job.ChannelDescriptionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.job.JobNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Create sync job statement for Doris.
 */
@Getter
@Setter
public final class DorisCreateSyncJobStatement extends DDLStatement {
    
    private JobNameSegment jobName;
    
    private final Collection<ChannelDescriptionSegment> channelDescriptions = new LinkedList<>();
    
    private BinlogDescriptionSegment binlogDescription;
    
    public DorisCreateSyncJobStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Get job name segment.
     *
     * @return job name segment
     */
    public Optional<JobNameSegment> getJobName() {
        return Optional.ofNullable(jobName);
    }
    
    /**
     * Get binlog description segment.
     *
     * @return binlog description segment
     */
    public Optional<BinlogDescriptionSegment> getBinlogDescription() {
        return Optional.ofNullable(binlogDescription);
    }
}
