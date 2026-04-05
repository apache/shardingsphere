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

package org.apache.shardingsphere.mcp.capability;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.metadata.model.MetadataObjectType;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Database capability builder support.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseCapabilityBuilderSupport {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?.*");
    
    /**
     * Create default database capability.
     *
     * @param databaseName logical database name
     * @param databaseType database type
     * @param transactionCapability transaction capability
     * @param indexSupported whether index metadata is supported
     * @param defaultSchemaSemantics schema semantics
     * @param crossSchemaQuerySupported whether cross-schema SQL is supported
     * @param supportsExplainAnalyze whether explain analyze is supported
     * @return database capability
     */
    public static DatabaseCapability createDefaultCapability(final String databaseName, final String databaseType, final TransactionCapability transactionCapability,
                                                             final boolean indexSupported, final SchemaSemantics defaultSchemaSemantics,
                                                             final boolean crossSchemaQuerySupported, final boolean supportsExplainAnalyze) {
        return new DatabaseCapability(databaseName, databaseType, "BASELINE", createSupportedMetadataObjectTypes(indexSupported),
                createSupportedStatementClasses(transactionCapability, supportsExplainAnalyze), TransactionCapability.NONE != transactionCapability,
                TransactionCapability.LOCAL_WITH_SAVEPOINT == transactionCapability, createSupportedTransactionStatements(transactionCapability),
                true, 1000, 30000, defaultSchemaSemantics, crossSchemaQuerySupported, supportsExplainAnalyze,
                TransactionBoundaryBehavior.NATIVE, TransactionBoundaryBehavior.NATIVE,
                supportsExplainAnalyze ? ResultBehavior.RESULT_SET : ResultBehavior.UNSUPPORTED,
                supportsExplainAnalyze ? TransactionBoundaryBehavior.NATIVE : TransactionBoundaryBehavior.UNSUPPORTED);
    }
    
    /**
     * Create supported transaction statements.
     *
     * @param transactionCapability transaction capability
     * @return supported transaction statements
     */
    public static Set<String> createSupportedTransactionStatements(final TransactionCapability transactionCapability) {
        Set<String> result = new LinkedHashSet<>(16, 1F);
        if (TransactionCapability.NONE != transactionCapability) {
            result.add("BEGIN");
            result.add("START TRANSACTION");
            result.add("COMMIT");
            result.add("ROLLBACK");
        }
        if (TransactionCapability.LOCAL_WITH_SAVEPOINT == transactionCapability) {
            result.add("SAVEPOINT");
            result.add("ROLLBACK TO SAVEPOINT");
            result.add("RELEASE SAVEPOINT");
        }
        return result;
    }
    
    /**
     * Judge whether database version is at least target version.
     *
     * @param databaseVersion database version
     * @param major target major version
     * @param minor target minor version
     * @param patch target patch version
     * @return whether the actual version is at least the target version
     */
    public static boolean isVersionAtLeast(final String databaseVersion, final int major, final int minor, final int patch) {
        Matcher matcher = VERSION_PATTERN.matcher(Objects.toString(databaseVersion, "").trim());
        if (!matcher.matches()) {
            return false;
        }
        int actualMajor = Integer.parseInt(matcher.group(1));
        int actualMinor = null == matcher.group(2) ? 0 : Integer.parseInt(matcher.group(2));
        int actualPatch = null == matcher.group(3) ? 0 : Integer.parseInt(matcher.group(3));
        if (actualMajor != major) {
            return actualMajor > major;
        }
        if (actualMinor != minor) {
            return actualMinor > minor;
        }
        return actualPatch >= patch;
    }
    
    private static Set<MetadataObjectType> createSupportedMetadataObjectTypes(final boolean indexSupported) {
        Set<MetadataObjectType> result = new LinkedHashSet<>(16, 1F);
        result.add(MetadataObjectType.SCHEMA);
        result.add(MetadataObjectType.TABLE);
        result.add(MetadataObjectType.VIEW);
        result.add(MetadataObjectType.COLUMN);
        if (indexSupported) {
            result.add(MetadataObjectType.INDEX);
        }
        return result;
    }
    
    private static Set<SupportedMCPStatement> createSupportedStatementClasses(final TransactionCapability transactionCapability, final boolean supportsExplainAnalyze) {
        Set<SupportedMCPStatement> result = new LinkedHashSet<>(16, 1F);
        result.add(SupportedMCPStatement.QUERY);
        result.add(SupportedMCPStatement.DML);
        result.add(SupportedMCPStatement.DDL);
        result.add(SupportedMCPStatement.DCL);
        if (TransactionCapability.NONE != transactionCapability) {
            result.add(SupportedMCPStatement.TRANSACTION_CONTROL);
        }
        if (TransactionCapability.LOCAL_WITH_SAVEPOINT == transactionCapability) {
            result.add(SupportedMCPStatement.SAVEPOINT);
        }
        if (supportsExplainAnalyze) {
            result.add(SupportedMCPStatement.EXPLAIN_ANALYZE);
        }
        return result;
    }
}
