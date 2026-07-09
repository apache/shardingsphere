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

package org.apache.shardingsphere.database.connector.core.metadata.data.loader.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sequence.DialectSequenceOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.SystemDatabase;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Sequence meta data loader.
 */
@RequiredArgsConstructor
public final class SequenceMetaDataLoader {
    
    private static final String SEQUENCE_SCHEMA = "SEQUENCE_SCHEMA";
    
    private static final String SEQUENCE_NAME = "SEQUENCE_NAME";
    
    private final DatabaseType databaseType;
    
    /**
     * Load sequence names by schema.
     *
     * @param connection connection
     * @return loaded sequence names by schema
     * @throws SQLException SQL exception
     */
    public Map<String, Collection<String>> load(final Connection connection) throws SQLException {
        Optional<DialectSequenceOption> sequenceOption = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSequenceOption();
        if (!sequenceOption.isPresent()) {
            return Collections.emptyMap();
        }
        return loadSequences(connection, sequenceOption.get().getSequenceMetadataQuery());
    }
    
    private Map<String, Collection<String>> loadSequences(final Connection connection, final String sequenceMetadataQuery) throws SQLException {
        Map<String, Collection<String>> result = new LinkedHashMap<>(16, 1F);
        try (Statement statement = connection.createStatement(); ResultSet sequences = statement.executeQuery(sequenceMetadataQuery)) {
            while (sequences.next()) {
                addSequence(result, sequences);
            }
        }
        return result;
    }
    
    private void addSequence(final Map<String, Collection<String>> sequences, final ResultSet resultSet) throws SQLException {
        String sequenceName = trimToEmpty(resultSet.getString(SEQUENCE_NAME));
        if (sequenceName.isEmpty()) {
            return;
        }
        String schemaName = trimToEmpty(resultSet.getString(SEQUENCE_SCHEMA));
        if (!schemaName.isEmpty() && isSystemSchema(schemaName)) {
            return;
        }
        sequences.computeIfAbsent(schemaName, unused -> new LinkedHashSet<>()).add(sequenceName);
    }
    
    private boolean isSystemSchema(final String schemaName) {
        for (String each : new SystemDatabase(databaseType).getSystemSchemas()) {
            if (schemaName.equalsIgnoreCase(each)) {
                return true;
            }
        }
        return false;
    }
    
    private String trimToEmpty(final String value) {
        return Objects.toString(value, "").trim();
    }
}
