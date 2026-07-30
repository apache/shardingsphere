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

package org.apache.shardingsphere.test.e2e.mcp.support.artifact;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * MCP E2E artifact utilities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPArtifactUtils {
    
    private static final String ARTIFACT_ROOT_PROPERTY = "mcp.e2e.artifact-root";
    
    private static final int MAX_RUNTIME_LOG_CHARS = 4096;
    
    private static final Pattern JSON_SECRET_FIELD_PATTERN = Pattern.compile(
            "(?i)(\"(?:api[_-]?key|access[_-]?token|token|authorization|password|passwd|pwd|secret)\"\\s*:\\s*\")([^\"]+)(\")");
    
    private static final Pattern SECRET_ASSIGNMENT_PATTERN = Pattern.compile(
            "(?i)((?:api[_-]?key|access[_-]?token|token|authorization|password|passwd|pwd|secret)\\s*[:=]\\s*[\"']?)([^\\s,\"'}]+)");
    
    private static final Pattern BEARER_TOKEN_PATTERN = Pattern.compile("(?i)(Bearer\\s+)[A-Za-z0-9._~+/=-]+");
    
    private static final Pattern JDBC_URL_PATTERN = Pattern.compile("(?i)jdbc:[^\\s\"']+");
    
    private static final Pattern URI_USER_INFO_PATTERN = Pattern.compile("(?i)([a-z][a-z0-9+.-]*://[^:/\\s\"']+:)[^@\\s\"']+(@)");
    
    /**
     * Redact secret-shaped fields and concrete sensitive values.
     *
     * @param value artifact value
     * @param sensitiveValues concrete sensitive values
     * @return redacted value
     */
    public static String redact(final String value, final Collection<String> sensitiveValues) {
        String result = value;
        for (String each : sensitiveValues) {
            if (!each.isBlank()) {
                result = result.replace(each, "<redacted>");
            }
        }
        result = JSON_SECRET_FIELD_PATTERN.matcher(result).replaceAll("$1<redacted>$3");
        result = SECRET_ASSIGNMENT_PATTERN.matcher(result).replaceAll("$1<redacted>");
        result = BEARER_TOKEN_PATTERN.matcher(result).replaceAll("$1<redacted>");
        result = JDBC_URL_PATTERN.matcher(result).replaceAll("<redacted-jdbc-url>");
        return URI_USER_INFO_PATTERN.matcher(result).replaceAll("$1<redacted>$2");
    }
    
    /**
     * Write one bounded and redacted runtime log when the artifact root is configured.
     *
     * @param filePrefix artifact file prefix
     * @param outputMessages runtime output messages
     */
    public static void writeRuntimeLogIfConfigured(final String filePrefix, final Collection<String> outputMessages) {
        String artifactRoot = System.getProperty(ARTIFACT_ROOT_PROPERTY, "").trim();
        if (artifactRoot.isEmpty() || outputMessages.isEmpty()) {
            return;
        }
        try {
            String content = String.join(System.lineSeparator(), outputMessages);
            String boundedContent = content.length() <= MAX_RUNTIME_LOG_CHARS
                    ? content
                    : "...<truncated>" + content.substring(content.length() - MAX_RUNTIME_LOG_CHARS);
            writeArtifact(Path.of(artifactRoot), filePrefix, boundedContent);
        } catch (final IOException ignored) {
        }
    }
    
    /**
     * Copy one bounded and redacted runtime log when the artifact root is configured.
     *
     * @param filePrefix artifact file prefix
     * @param runtimeLog runtime log
     */
    public static void copyRuntimeLogIfConfigured(final String filePrefix, final Path runtimeLog) {
        String artifactRoot = System.getProperty(ARTIFACT_ROOT_PROPERTY, "").trim();
        if (artifactRoot.isEmpty() || !Files.isRegularFile(runtimeLog)) {
            return;
        }
        try {
            writeArtifact(Path.of(artifactRoot), filePrefix, readBoundedTail(runtimeLog));
        } catch (final IOException ignored) {
        }
    }
    
    private static void writeArtifact(final Path artifactDirectory, final String filePrefix, final String content) throws IOException {
        Files.createDirectories(artifactDirectory);
        Files.writeString(Files.createTempFile(artifactDirectory, filePrefix, ".log"), redact(content, List.of()));
    }
    
    private static String readBoundedTail(final Path runtimeLog) throws IOException {
        long fileSize = Files.size(runtimeLog);
        int byteCount = (int) Math.min(fileSize, MAX_RUNTIME_LOG_CHARS);
        ByteBuffer buffer = ByteBuffer.allocate(byteCount);
        try (SeekableByteChannel channel = Files.newByteChannel(runtimeLog, StandardOpenOption.READ)) {
            channel.position(fileSize - byteCount);
            while (buffer.hasRemaining()) {
                if (0 >= channel.read(buffer)) {
                    break;
                }
            }
        }
        return new String(buffer.array(), StandardCharsets.UTF_8);
    }
}
