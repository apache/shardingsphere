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

package org.apache.shardingsphere.mcp.core.completion.provider;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProvider;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequestContext;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPIndexMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSequenceMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPTableMetadata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * Metadata completion provider.
 */
public final class MetadataCompletionProvider implements MCPCompletionProvider<MCPDatabaseHandlerContext> {
    
    private static final Set<String> SUPPORTED_ARGUMENTS = Set.of("database", "schema", "table", "column", "index", "sequence");
    
    @Override
    public Class<MCPDatabaseHandlerContext> getContextType() {
        return MCPDatabaseHandlerContext.class;
    }
    
    @Override
    public boolean supports(final MCPCompletionRequestContext requestContext) {
        return SUPPORTED_ARGUMENTS.contains(requestContext.getArgumentName());
    }
    
    @Override
    public MCPCompletionProviderResult complete(final MCPDatabaseHandlerContext handlerContext, final MCPCompletionRequestContext requestContext) {
        Map<String, String> contextArguments = new LinkedHashMap<>(requestContext.getContextArguments());
        Map<String, Object> inferredContextArguments = applySingleSchemaDefault(handlerContext, requestContext.getArgumentName(), contextArguments);
        mergeInferredContextArguments(contextArguments, inferredContextArguments);
        return new MCPCompletionProviderResult(completeMetadata(handlerContext, requestContext.getArgumentName(), contextArguments), inferredContextArguments);
    }
    
    private Map<String, Object> applySingleSchemaDefault(final MCPDatabaseHandlerContext handlerContext, final String argumentName, final Map<String, String> contextArguments) {
        if (!requiresSchemaContext(argumentName) || !Objects.toString(contextArguments.get("schema"), "").isEmpty()
                || Objects.toString(contextArguments.get("database"), "").isEmpty()) {
            return Map.of();
        }
        return handlerContext.getMetadataQueryFacade().queryDatabase(contextArguments.get("database"))
                .filter(each -> 1 == each.getSchemas().size())
                .map(each -> Map.<String, Object>of("schema", each.getSchemas().iterator().next().getSchema()))
                .orElseGet(Map::of);
    }
    
    private void mergeInferredContextArguments(final Map<String, String> contextArguments, final Map<String, Object> inferredContextArguments) {
        for (Entry<String, Object> entry : inferredContextArguments.entrySet()) {
            if (Objects.toString(contextArguments.get(entry.getKey()), "").isEmpty()) {
                contextArguments.put(entry.getKey(), Objects.toString(entry.getValue(), ""));
            }
        }
    }
    
    private boolean requiresSchemaContext(final String argumentName) {
        return "table".equals(argumentName) || "column".equals(argumentName) || "index".equals(argumentName) || "sequence".equals(argumentName);
    }
    
    private List<MCPCompletionCandidate> completeMetadata(final MCPDatabaseHandlerContext handlerContext, final String argumentName, final Map<String, String> contextArguments) {
        if ("database".equals(argumentName)) {
            return completeDatabases(handlerContext);
        }
        if ("schema".equals(argumentName)) {
            return completeSchemas(handlerContext, contextArguments);
        }
        if ("table".equals(argumentName)) {
            return completeTables(handlerContext, contextArguments);
        }
        if ("column".equals(argumentName)) {
            return completeColumns(handlerContext, contextArguments);
        }
        if ("index".equals(argumentName)) {
            return completeIndexes(handlerContext, contextArguments);
        }
        return "sequence".equals(argumentName) ? completeSequences(handlerContext, contextArguments) : List.of();
    }
    
    private List<MCPCompletionCandidate> completeDatabases(final MCPDatabaseHandlerContext handlerContext) {
        return handlerContext.getMetadataQueryFacade().queryDatabases().stream()
                .map(each -> new MCPCompletionCandidate(each.getDatabase(), String.format("%s %s", each.getDatabaseType(), each.getDatabaseVersion()), "metadata")).toList();
    }
    
    private List<MCPCompletionCandidate> completeSchemas(final MCPDatabaseHandlerContext handlerContext, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        return database.isEmpty() ? List.of()
                : handlerContext.getMetadataQueryFacade().querySchemas(database).stream().map(MCPSchemaMetadata::getSchema)
                        .map(each -> new MCPCompletionCandidate(each, "schema", "metadata")).toList();
    }
    
    private List<MCPCompletionCandidate> completeTables(final MCPDatabaseHandlerContext handlerContext, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = getSchema(contextArguments);
        return database.isEmpty() || schema.isEmpty() ? List.of()
                : handlerContext.getMetadataQueryFacade().queryTables(database, schema).stream().map(MCPTableMetadata::getTable)
                        .map(each -> new MCPCompletionCandidate(each, "logical table", "metadata")).toList();
    }
    
    private List<MCPCompletionCandidate> completeColumns(final MCPDatabaseHandlerContext handlerContext, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = getSchema(contextArguments);
        String table = contextArguments.getOrDefault("table", "");
        return database.isEmpty() || schema.isEmpty() || table.isEmpty() ? List.of()
                : handlerContext.getMetadataQueryFacade().queryTableColumns(database, schema, table).stream().map(MCPColumnMetadata::getColumn)
                        .map(each -> new MCPCompletionCandidate(each, "column", "metadata")).toList();
    }
    
    private List<MCPCompletionCandidate> completeIndexes(final MCPDatabaseHandlerContext handlerContext, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = getSchema(contextArguments);
        String table = contextArguments.getOrDefault("table", "");
        if (database.isEmpty() || schema.isEmpty() || table.isEmpty()) {
            return List.of();
        }
        try {
            return handlerContext.getMetadataQueryFacade().queryIndexes(database, schema, table).stream().map(MCPIndexMetadata::getIndex)
                    .map(each -> new MCPCompletionCandidate(each, "index", "metadata")).toList();
        } catch (final MCPUnsupportedException ignored) {
            return List.of();
        }
    }
    
    private List<MCPCompletionCandidate> completeSequences(final MCPDatabaseHandlerContext handlerContext, final Map<String, String> contextArguments) {
        String database = contextArguments.getOrDefault("database", "");
        String schema = getSchema(contextArguments);
        if (database.isEmpty() || schema.isEmpty()) {
            return List.of();
        }
        try {
            return handlerContext.getMetadataQueryFacade().querySequences(database, schema).stream().map(MCPSequenceMetadata::getSequence)
                    .map(each -> new MCPCompletionCandidate(each, "sequence", "metadata")).toList();
        } catch (final MCPUnsupportedException ignored) {
            return List.of();
        }
    }
    
    private String getSchema(final Map<String, String> contextArguments) {
        return Objects.toString(contextArguments.get("schema"), "");
    }
}
