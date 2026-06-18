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
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.load.BrokerLoadDataDescSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Broker load statement for Doris.
 */
@Getter
@Setter
public final class DorisBrokerLoadStatement extends DMLStatement {
    
    private DatabaseSegment database;
    
    private String loadLabel;
    
    private final Collection<BrokerLoadDataDescSegment> dataDescs = new LinkedList<>();
    
    private String brokerType;
    
    private String brokerName;
    
    private PropertiesSegment brokerProperties;
    
    private PropertiesSegment loadProperties;
    
    private String comment;
    
    public DorisBrokerLoadStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Get database.
     *
     * @return database segment
     */
    public Optional<DatabaseSegment> getDatabase() {
        return Optional.ofNullable(database);
    }
    
    /**
     * Get broker properties.
     *
     * @return broker properties segment
     */
    public Optional<PropertiesSegment> getBrokerProperties() {
        return Optional.ofNullable(brokerProperties);
    }
    
    /**
     * Get load properties.
     *
     * @return load properties segment
     */
    public Optional<PropertiesSegment> getLoadProperties() {
        return Optional.ofNullable(loadProperties);
    }
    
    /**
     * Get comment.
     *
     * @return comment
     */
    public Optional<String> getComment() {
        return Optional.ofNullable(comment);
    }
    
    /**
     * Get broker name.
     *
     * @return broker name
     */
    public Optional<String> getBrokerName() {
        return Optional.ofNullable(brokerName);
    }
}
