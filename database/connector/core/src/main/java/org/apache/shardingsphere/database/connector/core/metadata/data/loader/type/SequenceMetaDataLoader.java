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

import com.google.common.base.Strings;
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
        Optional<DialectSequenceOption> option = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSequenceOption();
        return option.isPresent() ? loadSequences(connection, option.get().getSequenceMetadataQuery()) : Collections.emptyMap();
    }
    
    private Map<String, Collection<String>> loadSequences(final Connection connection, final String sequenceMetadataQuery) throws SQLException {
        Map<String, Collection<String>> result = new LinkedHashMap<>(16, 1F);
        Collection<String> systemSchemas = new SystemDatabase(databaseType).getSystemSchemas();
        try (
                Statement statement = connection.createStatement();
                ResultSet sequences = statement.executeQuery(sequenceMetadataQuery)) {
            while (sequences.next()) {
                addSequence(result, sequences, systemSchemas);
            }
        }
        return result;
    }
    
    private void addSequence(final Map<String, Collection<String>> sequences, final ResultSet resultSet, final Collection<String> systemSchemas) throws SQLException {
        String sequenceName = resultSet.getString(SEQUENCE_NAME);
        if (Strings.isNullOrEmpty(sequenceName)) {
            return;
        }
        String schemaName = resultSet.getString(SEQUENCE_SCHEMA);
        if (Strings.isNullOrEmpty(schemaName) || systemSchemas.stream().noneMatch(schemaName::equalsIgnoreCase)) {
            sequences.computeIfAbsent(schemaName, unused -> new LinkedHashSet<>()).add(sequenceName);
        }
    }
}
