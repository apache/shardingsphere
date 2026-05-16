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

package org.apache.shardingsphere.mcp.bootstrap.registry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
            require(!options.version().isBlank() && !options.identifier().isBlank(), "--version and --identifier are required unless --validate-only is set.");
            prepareServerJson(server, options.version(), options.identifier());
        }
        validateServerJson(server, options.allowSnapshot());
        if (!options.validateOnly()) {
            Files.writeString(options.path(), JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(server) + System.lineSeparator());
        }
    }
    
    private static CommandOptions parseOptions(final String[] args) {
        Path path = Path.of("mcp/server.json");
        String version = "";
        String identifier = "";
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
        return new CommandOptions(path, version, identifier, validateOnly, allowSnapshot);
    }
    
    private static String readOptionValue(final String[] args, final int index, final String optionName) {
        require(index < args.length && !args[index].startsWith("--"), optionName + " requires a value.");
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
        require(SCHEMA_URL.equals(server.get("$schema")), "server.json must use the official MCP Registry schema.");
        require(SERVER_NAME.equals(server.get("name")), "server.json name must match the published ShardingSphere MCP server name.");
        requireString(server, "description", 100);
        requireString(server, "version", 255);
        validateVersion("server version", String.valueOf(server.get("version")), allowSnapshot);
        List<Map<String, Object>> packages = getPackages(server);
        require(SUPPORTED_TRANSPORTS.size() == packages.size(), PACKAGE_SHAPE_ERROR_MESSAGE);
        Set<String> transportTypes = new LinkedHashSet<>(packages.size(), 1F);
        for (Map<String, Object> each : packages) {
            String transportType = validatePackage(each, String.valueOf(server.get("version")), allowSnapshot);
            require(transportTypes.add(transportType), PACKAGE_SHAPE_ERROR_MESSAGE);
        }
        require(SUPPORTED_TRANSPORTS.equals(transportTypes), PACKAGE_SHAPE_ERROR_MESSAGE);
    }
    
    private static String validatePackage(final Map<String, Object> packageMetadata, final String serverVersion, final boolean allowSnapshot) {
        require("oci".equals(packageMetadata.get("registryType")), "MCP Registry package registryType must be oci.");
        String identifier = String.valueOf(packageMetadata.get("identifier"));
        require(OCI_IDENTIFIER_PATTERN.matcher(identifier).matches(), "OCI identifier must target ghcr.io/apache/shardingsphere-mcp:<tag>.");
        validateVersion("package identifier", identifier, allowSnapshot);
        require(identifier.endsWith(":" + serverVersion), "OCI identifier tag must match the server version.");
        if (packageMetadata.containsKey("version")) {
            validateVersion("package version", String.valueOf(packageMetadata.get("version")), allowSnapshot);
            require(serverVersion.equals(packageMetadata.get("version")), "MCP Registry package version must match the server version.");
        }
        Map<String, Object> transport = asMap(packageMetadata.get("transport"), "MCP Registry package transport must be an object.");
        String transportType = String.valueOf(transport.get("type"));
        require(SUPPORTED_TRANSPORTS.contains(transportType), "MCP Registry package transport type must be stdio or streamable-http.");
        if ("streamable-http".equals(transportType)) {
            requireHttpUrl(transport.get("url"));
        }
        requireEnvironmentVariable(packageMetadata, "SHARDINGSPHERE_MCP_TRANSPORT");
        requireEnvironmentVariable(packageMetadata, "SHARDINGSPHERE_MCP_CONFIG");
        return transportType;
    }
    
    private static List<Map<String, Object>> getPackages(final Map<String, Object> server) {
        Object packages = server.get("packages");
        require(packages instanceof List<?>, "server.json packages must be a non-empty array.");
        return ((List<?>) packages).stream().map(each -> asMap(each, "MCP Registry package must be an object.")).toList();
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(final Object value, final String message) {
        require(value instanceof Map<?, ?>, message);
        return (Map<String, Object>) value;
    }
    
    private static void requireString(final Map<String, Object> target, final String key, final int maxLength) {
        Object value = target.get(key);
        require(value instanceof String && !((String) value).isBlank(), "server.json field " + key + " must be a non-empty string.");
        require(((String) value).length() <= maxLength, "server.json field " + key + " must be at most " + maxLength + " characters.");
    }
    
    private static void validateVersion(final String label, final String value, final boolean allowSnapshot) {
        require(!value.isBlank() && !"null".equals(value), label + " must be a non-empty string.");
        require(!"latest".equals(value), label + " must not use latest.");
        require(!VERSION_RANGE_PATTERN.matcher(value).matches(), label + " must be a specific version, not a range.");
        if (!allowSnapshot) {
            require(!value.contains("SNAPSHOT"), label + " must not contain SNAPSHOT for publication.");
        }
    }
    
    private static void requireHttpUrl(final Object value) {
        require(value instanceof String && !((String) value).isBlank(), "streamable-http transport must define a URL.");
        try {
            URI uri = new URI((String) value);
            require(("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) && null != uri.getHost(), "streamable-http transport URL must be an HTTP URL.");
        } catch (final URISyntaxException ex) {
            throw new IllegalArgumentException("streamable-http transport URL must be an HTTP URL.", ex);
        }
    }
    
    private static void requireEnvironmentVariable(final Map<String, Object> packageMetadata, final String name) {
        Object envVars = packageMetadata.get("environmentVariables");
        require(envVars instanceof List<?>, "MCP Registry package must define " + name + ".");
        require(((List<?>) envVars).stream().filter(each -> each instanceof Map<?, ?>).map(each -> (Map<?, ?>) each).anyMatch(each -> name.equals(each.get("name"))),
                "MCP Registry package must define " + name + ".");
    }
    
    private static void require(final boolean condition, final String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
    
    private record CommandOptions(Path path, String version, String identifier, boolean validateOnly, boolean allowSnapshot) {
    }
}
