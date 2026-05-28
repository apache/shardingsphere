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

package org.apache.shardingsphere.mcp.registry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * MCP Registry metadata command.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPRegistryMetadataCommand {
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    private static final String SCHEMA_URL = "https://static.modelcontextprotocol.io/schemas/2025-12-11/server.schema.json";
    
    private static final String SERVER_NAME = "io.github.apache/shardingsphere-mcp";
    
    private static final Pattern OCI_IDENTIFIER_PATTERN = Pattern.compile("^ghcr\\.io/apache/shardingsphere-mcp:[^:\\s]+$");
    
    private static final Pattern VERSION_RANGE_PATTERN = Pattern.compile("^(?:[\\^~]|[<>]=?|.*(?:\\*|\\.x|\\s-\\s|\\|\\|).*)");
    
    private static final Set<String> SUPPORTED_TRANSPORTS = Set.of("stdio", "streamable-http");
    
    private static final String PACKAGE_SHAPE_ERROR_MESSAGE = "server.json packages must contain exactly one stdio OCI package and one streamable-http OCI package.";
    
    /**
     * Main entrance.
     *
     * @param args command arguments
     * @throws IOException when server.json cannot be read or written
     */
    // CHECKSTYLE:OFF
    public static void main(final String[] args) throws IOException {
        // CHECKSTYLE:ON
        try {
            execute(args);
        } catch (final IllegalArgumentException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * Execute metadata preparation or validation.
     *
     * @param args command arguments
     * @throws IOException when server.json cannot be read or written
     */
    public static void execute(final String... args) throws IOException {
        CommandOptions options = parseOptions(args);
        Map<String, Object> server = JSON_MAPPER.readValue(options.path().toFile(), new TypeReference<>() {
        });
        if (!options.validateOnly()) {
            ShardingSpherePreconditions.checkState(!options.version().isBlank() && !options.identifier().isBlank(),
                    () -> new IllegalArgumentException("--version and --identifier are required unless --validate-only is set."));
            prepareServerJson(server, options.version(), options.identifier());
        }
        validateServerJson(server, options.allowSnapshot());
        if (!options.dockerfilePath().isBlank()) {
            MCPDockerfileMetadataValidator.validate(Path.of(options.dockerfilePath()), String.valueOf(server.get("name")));
        }
        if (!options.validateOnly()) {
            Files.writeString(options.path(), JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(server) + System.lineSeparator());
        }
    }
    
    private static CommandOptions parseOptions(final String[] args) {
        Path path = Path.of("mcp/server.json");
        String version = "";
        String identifier = "";
        String dockerfilePath = "";
        boolean validateOnly = false;
        boolean allowSnapshot = false;
        int index = 0;
        while (index < args.length) {
            switch (args[index]) {
                case "--path":
                    path = Path.of(readOptionValue(args, index + 1, "--path"));
                    index += 2;
                    break;
                case "--version":
                    version = readOptionValue(args, index + 1, "--version");
                    index += 2;
                    break;
                case "--identifier":
                    identifier = readOptionValue(args, index + 1, "--identifier");
                    index += 2;
                    break;
                case "--dockerfile-path":
                    dockerfilePath = readOptionValue(args, index + 1, "--dockerfile-path");
                    index += 2;
                    break;
                case "--validate-only":
                    validateOnly = true;
                    index++;
                    break;
                case "--allow-snapshot":
                    allowSnapshot = true;
                    index++;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument: " + args[index]);
            }
        }
        return new CommandOptions(path, version, identifier, dockerfilePath, validateOnly, allowSnapshot);
    }
    
    private static String readOptionValue(final String[] args, final int index, final String optionName) {
        ShardingSpherePreconditions.checkState(index < args.length && !args[index].startsWith("--"),
                () -> new IllegalArgumentException(optionName + " requires a value."));
        return args[index];
    }
    
    private static void prepareServerJson(final Map<String, Object> server, final String version, final String identifier) {
        server.put("version", version);
        for (Map<String, Object> each : getPackages(server)) {
            each.put("identifier", identifier);
            each.put("version", version);
        }
    }
    
    private static void validateServerJson(final Map<String, Object> server, final boolean allowSnapshot) {
        ShardingSpherePreconditions.checkState(SCHEMA_URL.equals(server.get("$schema")), () -> new IllegalArgumentException("server.json must use the official MCP Registry schema."));
        ShardingSpherePreconditions.checkState(SERVER_NAME.equals(server.get("name")),
                () -> new IllegalArgumentException("server.json name must match the published ShardingSphere MCP server name."));
        validateString(server, "description", 100);
        validateString(server, "version", 255);
        validateVersion("server version", String.valueOf(server.get("version")), allowSnapshot);
        List<Map<String, Object>> packages = getPackages(server);
        ShardingSpherePreconditions.checkState(SUPPORTED_TRANSPORTS.size() == packages.size(), () -> new IllegalArgumentException(PACKAGE_SHAPE_ERROR_MESSAGE));
        Set<String> transportTypes = new LinkedHashSet<>(packages.size(), 1F);
        for (Map<String, Object> each : packages) {
            String transportType = validatePackage(each, String.valueOf(server.get("version")), allowSnapshot);
            ShardingSpherePreconditions.checkState(transportTypes.add(transportType), () -> new IllegalArgumentException(PACKAGE_SHAPE_ERROR_MESSAGE));
        }
        ShardingSpherePreconditions.checkState(SUPPORTED_TRANSPORTS.equals(transportTypes), () -> new IllegalArgumentException(PACKAGE_SHAPE_ERROR_MESSAGE));
    }
    
    private static String validatePackage(final Map<String, Object> packageMetadata, final String serverVersion, final boolean allowSnapshot) {
        ShardingSpherePreconditions.checkState("oci".equals(packageMetadata.get("registryType")),
                () -> new IllegalArgumentException("MCP Registry package registryType must be oci."));
        String identifier = String.valueOf(packageMetadata.get("identifier"));
        ShardingSpherePreconditions.checkState(OCI_IDENTIFIER_PATTERN.matcher(identifier).matches(),
                () -> new IllegalArgumentException("OCI identifier must target ghcr.io/apache/shardingsphere-mcp:<tag>."));
        validateVersion("package identifier", identifier, allowSnapshot);
        ShardingSpherePreconditions.checkState(identifier.endsWith(":" + serverVersion), () -> new IllegalArgumentException("OCI identifier tag must match the server version."));
        if (packageMetadata.containsKey("version")) {
            validateVersion("package version", String.valueOf(packageMetadata.get("version")), allowSnapshot);
            ShardingSpherePreconditions.checkState(serverVersion.equals(packageMetadata.get("version")),
                    () -> new IllegalArgumentException("MCP Registry package version must match the server version."));
        }
        Map<String, Object> transport = asMap(packageMetadata.get("transport"), "MCP Registry package transport must be an object.");
        String transportType = String.valueOf(transport.get("type"));
        ShardingSpherePreconditions.checkState(SUPPORTED_TRANSPORTS.contains(transportType),
                () -> new IllegalArgumentException("MCP Registry package transport type must be stdio or streamable-http."));
        if ("streamable-http".equals(transportType)) {
            validateHttpUrl(transport.get("url"));
        }
        validateEnvironmentVariable(packageMetadata, "SHARDINGSPHERE_MCP_TRANSPORT");
        validateEnvironmentVariable(packageMetadata, "SHARDINGSPHERE_MCP_CONFIG");
        return transportType;
    }
    
    private static List<Map<String, Object>> getPackages(final Map<String, Object> server) {
        Object packages = server.get("packages");
        ShardingSpherePreconditions.checkState(packages instanceof List<?>, () -> new IllegalArgumentException("server.json packages must be a non-empty array."));
        return ((List<?>) packages).stream().map(each -> asMap(each, "MCP Registry package must be an object.")).toList();
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(final Object value, final String message) {
        ShardingSpherePreconditions.checkState(value instanceof Map<?, ?>, () -> new IllegalArgumentException(message));
        return (Map<String, Object>) value;
    }
    
    private static void validateString(final Map<String, Object> target, final String key, final int maxLength) {
        Object value = target.get(key);
        ShardingSpherePreconditions.checkState(value instanceof String && !((String) value).isBlank(),
                () -> new IllegalArgumentException("server.json field " + key + " must be a non-empty string."));
        ShardingSpherePreconditions.checkState(((String) value).length() <= maxLength,
                () -> new IllegalArgumentException("server.json field " + key + " must be at most " + maxLength + " characters."));
    }
    
    private static void validateVersion(final String label, final String value, final boolean allowSnapshot) {
        ShardingSpherePreconditions.checkState(!value.isBlank() && !"null".equals(value), () -> new IllegalArgumentException(label + " must be a non-empty string."));
        ShardingSpherePreconditions.checkState(!"latest".equals(value), () -> new IllegalArgumentException(label + " must not use latest."));
        ShardingSpherePreconditions.checkState(!VERSION_RANGE_PATTERN.matcher(value).matches(),
                () -> new IllegalArgumentException(label + " must be a specific version, not a range."));
        if (!allowSnapshot) {
            ShardingSpherePreconditions.checkState(!value.contains("SNAPSHOT"), () -> new IllegalArgumentException(label + " must not contain SNAPSHOT for publication."));
        }
    }
    
    private static void validateHttpUrl(final Object value) {
        ShardingSpherePreconditions.checkState(value instanceof String && !((String) value).isBlank(),
                () -> new IllegalArgumentException("streamable-http transport must define a URL."));
        try {
            URI uri = new URI((String) value);
            ShardingSpherePreconditions.checkState(("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) && null != uri.getHost(),
                    () -> new IllegalArgumentException("streamable-http transport URL must be an HTTP URL."));
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException("streamable-http transport URL must be an HTTP URL.", ex);
        }
    }
    
    private static void validateEnvironmentVariable(final Map<String, Object> packageMetadata, final String name) {
        Object envVars = packageMetadata.get("environmentVariables");
        ShardingSpherePreconditions.checkState(envVars instanceof List<?>, () -> new IllegalArgumentException("MCP Registry package must define " + name + "."));
        Map<?, ?> envVar = findEnvironmentVariable((List<?>) envVars, name);
        ShardingSpherePreconditions.checkState(Boolean.FALSE.equals(envVar.get("isRequired")),
                () -> new IllegalArgumentException(String.format("MCP Registry metadata for %s must declare isRequired as false.", name)));
        ShardingSpherePreconditions.checkState(Boolean.FALSE.equals(envVar.get("isSecret")),
                () -> new IllegalArgumentException(String.format("MCP Registry metadata for %s must declare isSecret as false.", name)));
        ShardingSpherePreconditions.checkState("string".equals(envVar.get("format")),
                () -> new IllegalArgumentException(String.format("MCP Registry metadata for %s format must be string.", name)));
    }
    
    private static Map<?, ?> findEnvironmentVariable(final List<?> envVars, final String name) {
        return envVars.stream().filter(each -> each instanceof Map<?, ?>).map(each -> (Map<?, ?>) each).filter(each -> name.equals(each.get("name"))).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("MCP Registry package must define " + name + "."));
    }
    
    private record CommandOptions(Path path, String version, String identifier, String dockerfilePath, boolean validateOnly, boolean allowSnapshot) {
    }
}
