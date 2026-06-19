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

package org.apache.shardingsphere.infra.metadata.database.schema.util;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Index meta data utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IndexMetaDataUtils {
    
    private static final String UNDERLINE = "_";
    
    private static final String SHORTENED_INDEX_SUFFIX_PREFIX = "_s";
    
    private static final String HASHED_INDEX_SUFFIX_PREFIX = "_h";
    
    private static final String TRUNCATED_INDEX_SUFFIX_PREFIX = "_t";
    
    private static final String HASH_ALGORITHM = "SHA-256";
    
    private static final int SHORTENED_INDEX_SUFFIX_HASH_BYTES = 5;
    
    private static final int LENGTH_SAFE_INDEX_SUFFIX_HASH_LENGTH = 8;
    
    private static final Pattern HASHED_INDEX_NAME_SUFFIX_PATTERN = Pattern.compile("_h[0-9a-z]{8}$");
    
    private static final Pattern TRUNCATED_INDEX_NAME_SUFFIX_PATTERN = Pattern.compile("_t[0-9a-z]{8}$");
    
    private static final int POSTGRESQL_INDEX_NAME_MAX_LENGTH = 63;
    
    private static final int OPENGAUSS_INDEX_NAME_MAX_LENGTH = 63;
    
    private static final int ORACLE_INDEX_NAME_MAX_LENGTH = 30;
    
    /**
     * Get logic index name.
     *
     * @param actualIndexName actual index name
     * @param actualTableName actual table name
     * @return logic index name
     */
    public static String getLogicIndexName(final String actualIndexName, final String actualTableName) {
        return stripActualIndexNameSuffix(actualIndexName, getLegacyActualIndexNameSuffix(actualTableName));
    }
    
    /**
     * Get actual index name.
     *
     * @param logicIndexName logic index name
     * @param actualTableName actual table name
     * @return actual index name
     */
    public static String getActualIndexName(final String logicIndexName, final String actualTableName) {
        return getLegacyActualIndexName(logicIndexName, actualTableName);
    }
    
    /**
     * Get actual index name with database identifier budget.
     *
     * @param logicIndexName logic index name
     * @param actualTableName actual table name
     * @param databaseType database type
     * @return actual index name
     */
    public static String getActualIndexName(final String logicIndexName, final String actualTableName, final DatabaseType databaseType) {
        if (Strings.isNullOrEmpty(actualTableName)) {
            return logicIndexName;
        }
        String legacyActualIndexName = getLegacyActualIndexName(logicIndexName, actualTableName);
        int maxLength = getIndexNameMaxLength(databaseType);
        if (getUtf8Length(legacyActualIndexName) <= maxLength) {
            return legacyActualIndexName;
        }
        String hashedActualIndexName = logicIndexName + getHashedActualIndexNameSuffix(logicIndexName, actualTableName);
        if (getUtf8Length(hashedActualIndexName) <= maxLength) {
            return hashedActualIndexName;
        }
        String truncatedSuffix = getTruncatedActualIndexNameSuffix(logicIndexName, actualTableName);
        return truncateToUtf8Bytes(logicIndexName, Math.max(0, maxLength - getUtf8Length(truncatedSuffix))) + truncatedSuffix;
    }
    
    /**
     * Get shortened actual index name for compatibility with the previous shortened suffix format.
     *
     * @param logicIndexName logic index name
     * @param actualTableName actual table name
     * @return shortened actual index name
     */
    public static String getShortenedActualIndexName(final String logicIndexName, final String actualTableName) {
        return Strings.isNullOrEmpty(actualTableName) ? logicIndexName : logicIndexName + getShortenedActualIndexNameSuffix(actualTableName);
    }
    
    /**
     * Get legacy actual index name.
     *
     * @param logicIndexName logic index name
     * @param actualTableName actual table name
     * @return legacy actual index name
     */
    public static String getLegacyActualIndexName(final String logicIndexName, final String actualTableName) {
        return Strings.isNullOrEmpty(actualTableName) ? logicIndexName : logicIndexName + getLegacyActualIndexNameSuffix(actualTableName);
    }
    
    /**
     * Get logic index name from generated actual index name.
     *
     * @param actualIndexName actual index name
     * @param actualTableName actual table name
     * @return logic index name
     */
    public static String getGeneratedLogicIndexName(final String actualIndexName, final String actualTableName) {
        if (HASHED_INDEX_NAME_SUFFIX_PATTERN.matcher(actualIndexName).find()) {
            return actualIndexName.substring(0, actualIndexName.length() - getLengthSafeGeneratedSuffixLength());
        }
        if (TRUNCATED_INDEX_NAME_SUFFIX_PATTERN.matcher(actualIndexName).find()) {
            return actualIndexName.substring(0, actualIndexName.length() - getLengthSafeGeneratedSuffixLength());
        }
        String result = stripActualIndexNameSuffix(actualIndexName, getShortenedActualIndexNameSuffix(actualTableName));
        return result.equals(actualIndexName) ? getLogicIndexName(actualIndexName, actualTableName) : result;
    }
    
    /**
     * Find logic index name from generated actual index name.
     *
     * @param actualIndexName actual index name
     * @param actualTableName actual table name
     * @param candidateLogicIndexNames candidate logic index names
     * @return matched logic index name
     */
    public static Optional<String> findGeneratedLogicIndexName(final String actualIndexName, final String actualTableName, final Collection<String> candidateLogicIndexNames) {
        if (candidateLogicIndexNames.contains(actualIndexName)) {
            return Optional.of(actualIndexName);
        }
        Optional<String> result = candidateLogicIndexNames.stream().filter(each -> isGeneratedActualIndexNameMatch(actualIndexName, each, actualTableName)).findFirst();
        if (result.isPresent()) {
            return result;
        }
        String generatedLogicIndexName = getGeneratedLogicIndexName(actualIndexName, actualTableName);
        if (isProvablyGeneratedActualIndexName(actualIndexName, generatedLogicIndexName, actualTableName)) {
            return Optional.of(generatedLogicIndexName);
        }
        return Optional.of(actualIndexName);
    }
    
    /**
     * Check whether actual index name matches generated format of the logic index.
     *
     * @param actualIndexName actual index name
     * @param logicIndexName logic index name
     * @param actualTableName actual table name
     * @return whether actual index name matches generated format or not
     */
    public static boolean isGeneratedActualIndexNameMatch(final String actualIndexName, final String logicIndexName, final String actualTableName) {
        return actualIndexName.equals(getLegacyActualIndexName(logicIndexName, actualTableName))
                || actualIndexName.equals(getShortenedActualIndexName(logicIndexName, actualTableName))
                || (logicIndexName + getHashedActualIndexNameSuffix(logicIndexName, actualTableName)).equals(actualIndexName)
                || isTruncatedActualIndexNameMatch(actualIndexName, logicIndexName, actualTableName);
    }
    
    /**
     * Get table names.
     *
     * @param database database
     * @param indexes indexes
     * @param protocolType protocol type
     * @return table names
     */
    public static Collection<QualifiedTable> getTableNames(final ShardingSphereDatabase database, final DatabaseType protocolType, final Collection<IndexSegment> indexes) {
        Collection<QualifiedTable> result = new LinkedList<>();
        String schemaName = new DatabaseTypeRegistry(protocolType).getDefaultSchemaName(database.getName());
        for (IndexSegment each : indexes) {
            String actualSchemaName = each.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(schemaName);
            findLogicTableNameFromMetaData(database.getSchema(actualSchemaName), each.getIndexName().getIdentifier())
                    .ifPresent(optional -> result.add(new QualifiedTable(actualSchemaName, optional)));
        }
        return result;
    }
    
    private static Optional<String> findLogicTableNameFromMetaData(final ShardingSphereSchema schema, final IdentifierValue logicIndexName) {
        return schema.getAllTables().stream().filter(table -> table.containsIndex(logicIndexName)).findFirst().map(ShardingSphereTable::getName);
    }
    
    private static String getLegacyActualIndexNameSuffix(final String actualTableName) {
        return Strings.isNullOrEmpty(actualTableName) ? "" : UNDERLINE + actualTableName;
    }
    
    private static String getShortenedActualIndexNameSuffix(final String actualTableName) {
        return Strings.isNullOrEmpty(actualTableName) ? "" : SHORTENED_INDEX_SUFFIX_PREFIX + shortenActualTableName(actualTableName);
    }
    
    private static String getHashedActualIndexNameSuffix(final String logicIndexName, final String actualTableName) {
        return HASHED_INDEX_SUFFIX_PREFIX + shortenIndexIdentity(logicIndexName, actualTableName);
    }
    
    private static String getTruncatedActualIndexNameSuffix(final String logicIndexName, final String actualTableName) {
        return TRUNCATED_INDEX_SUFFIX_PREFIX + shortenIndexIdentity(logicIndexName, actualTableName);
    }
    
    private static String stripActualIndexNameSuffix(final String actualIndexName, final String indexNameSuffix) {
        return Strings.isNullOrEmpty(indexNameSuffix) || !actualIndexName.endsWith(indexNameSuffix)
                ? actualIndexName
                : actualIndexName.substring(0, actualIndexName.length() - indexNameSuffix.length());
    }
    
    private static String shortenActualTableName(final String actualTableName) {
        return toVariableLengthRadixHash(digest(actualTableName));
    }
    
    private static String shortenIndexIdentity(final String logicIndexName, final String actualTableName) {
        return toFixedLengthRadixHash(digest(logicIndexName + '\0' + actualTableName));
    }
    
    private static String toVariableLengthRadixHash(final byte[] digest) {
        long result = 0L;
        for (int i = 0; i < SHORTENED_INDEX_SUFFIX_HASH_BYTES; i++) {
            result = result << Byte.SIZE | digest[i] & 0xFF;
        }
        return Long.toString(result, Character.MAX_RADIX);
    }
    
    private static String toFixedLengthRadixHash(final byte[] digest) {
        long result = 0L;
        for (int i = 0; i < SHORTENED_INDEX_SUFFIX_HASH_BYTES; i++) {
            result = result << Byte.SIZE | digest[i] & 0xFF;
        }
        String value = Long.toString(result, Character.MAX_RADIX);
        return Strings.padStart(value, LENGTH_SAFE_INDEX_SUFFIX_HASH_LENGTH, '0');
    }
    
    private static boolean isTruncatedActualIndexNameMatch(final String actualIndexName, final String logicIndexName, final String actualTableName) {
        String suffix = getTruncatedActualIndexNameSuffix(logicIndexName, actualTableName);
        if (!actualIndexName.endsWith(suffix)) {
            return false;
        }
        String actualPrefix = actualIndexName.substring(0, actualIndexName.length() - suffix.length());
        return actualPrefix.equals(truncateToUtf8Bytes(logicIndexName, getUtf8Length(actualPrefix)));
    }
    
    private static boolean isProvablyGeneratedActualIndexName(final String actualIndexName, final String generatedLogicIndexName, final String actualTableName) {
        return !generatedLogicIndexName.equals(actualIndexName) && (getLegacyActualIndexName(generatedLogicIndexName, actualTableName).equals(actualIndexName)
                || getShortenedActualIndexName(generatedLogicIndexName, actualTableName).equals(actualIndexName)
                || (generatedLogicIndexName + getHashedActualIndexNameSuffix(generatedLogicIndexName, actualTableName)).equals(actualIndexName));
    }
    
    private static int getIndexNameMaxLength(final DatabaseType databaseType) {
        if (null == databaseType) {
            return Integer.MAX_VALUE;
        }
        switch (databaseType.getType()) {
            case "PostgreSQL":
                return POSTGRESQL_INDEX_NAME_MAX_LENGTH;
            case "openGauss":
                return OPENGAUSS_INDEX_NAME_MAX_LENGTH;
            case "Oracle":
                return ORACLE_INDEX_NAME_MAX_LENGTH;
            default:
                return Integer.MAX_VALUE;
        }
    }
    
    private static int getLengthSafeGeneratedSuffixLength() {
        return HASHED_INDEX_SUFFIX_PREFIX.length() + LENGTH_SAFE_INDEX_SUFFIX_HASH_LENGTH;
    }
    
    private static String truncateToUtf8Bytes(final String value, final int maxBytes) {
        if (Strings.isNullOrEmpty(value) || maxBytes <= 0 || getUtf8Length(value) <= maxBytes) {
            return maxBytes <= 0 ? "" : value;
        }
        int endIndex = 0;
        while (endIndex < value.length()) {
            int nextEndIndex = value.offsetByCodePoints(endIndex, 1);
            if (getUtf8Length(value.substring(0, nextEndIndex)) > maxBytes) {
                break;
            }
            endIndex = nextEndIndex;
        }
        return value.substring(0, endIndex);
    }
    
    private static int getUtf8Length(final String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }
    
    private static byte[] digest(final String value) {
        try {
            return MessageDigest.getInstance(HASH_ALGORITHM).digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException(String.format("Could not find message digest algorithm `%s`.", HASH_ALGORITHM), ex);
        }
    }
}
