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

package org.apache.shardingsphere.test.e2e.mcp.support.distribution;

import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionTestSupport.PreparedPackagedDistribution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Packaged distribution process support.
 */
public final class PackagedDistributionProcessSupport implements AutoCloseable {
    
    private static final long PROCESS_STOP_TIMEOUT_SECONDS = 5L;
    
    private static final String JAVA_COMMAND_NAME = "java";
    
    private static final String MAIN_CLASS_NAME = "org.apache.shardingsphere.mcp.bootstrap.MCPBootstrap";
    
    private static final String UNIX_CLASS_PATH_SEPARATOR = ":";
    
    private static final String WINDOWS_CLASS_PATH_SEPARATOR = ";";
    
    private static final String WINDOWS_OS_NAME_PREFIX = "windows";
    
    private final Path distributionHome;
    
    private final Path configFile;
    
    @SuppressWarnings("UseOfProcessBuilder")
    private final ProcessBuilder processBuilder;
    
    private final String outputCollectorThreadName;
    
    private final List<String> outputMessages = new CopyOnWriteArrayList<>();
    
    private Process process;
    
    private Thread outputCollector;
    
    /**
     * Construct process support for a prepared packaged distribution.
     *
     * @param distribution prepared packaged distribution
     * @param outputCollectorThreadName output collector thread name
     */
    public PackagedDistributionProcessSupport(final PreparedPackagedDistribution distribution, final String outputCollectorThreadName) {
        distributionHome = distribution.home();
        configFile = distribution.configFile();
        processBuilder = createProcessBuilder(distributionHome, configFile);
        processBuilder.redirectErrorStream(true);
        this.outputCollectorThreadName = outputCollectorThreadName;
    }
    
    /**
     * Create a process builder for starting packaged distribution scripts.
     *
     * @param distributionHome packaged distribution home
     * @param configFile runtime config file
     * @return process builder
     */
    public static ProcessBuilder createProcessBuilder(final Path distributionHome, final Path configFile) {
        @SuppressWarnings("UseOfProcessBuilder")
        ProcessBuilder result = new ProcessBuilder(createCommand(distributionHome, configFile, System.getProperty("os.name", ""), System.getProperty("java.home", "")));
        result.directory(distributionHome.toFile());
        return result;
    }
    
    static List<String> createCommand(final Path distributionHome, final Path configFile, final String osName, final String javaHome) {
        return List.of(resolveJavaCommand(osName, javaHome),
                "-DAPP_HOME=" + distributionHome,
                "-Dlogback.configurationFile=" + distributionHome.resolve("conf/logback.xml"),
                "-cp", createClassPath(distributionHome, osName),
                MAIN_CLASS_NAME, configFile.toString());
    }
    
    static String createClassPath(final Path distributionHome, final String osName) {
        return String.join(getClassPathSeparator(osName),
                distributionHome.resolve("conf").toString(),
                distributionHome.resolve("lib").resolve("*").toString(),
                distributionHome.resolve("plugins").resolve("*").toString());
    }
    
    static Path resolveStartScript(final Path distributionHome) {
        return resolveStartScript(distributionHome, System.getProperty("os.name", ""));
    }
    
    static Path resolveStartScript(final Path distributionHome, final String osName) {
        return distributionHome.resolve(isWindows(osName) ? "bin/start.bat" : "bin/start.sh");
    }
    
    /**
     * Start the packaged distribution process when needed.
     *
     * @throws IOException IO exception
     */
    public void startIfNeeded() throws IOException {
        if (null == process) {
            prepareRuntimeLayout(distributionHome, configFile);
            process = processBuilder.start();
            outputCollector = startOutputCollector(process, outputCollectorThreadName, outputMessages);
        }
    }
    
    public boolean isAlive() {
        return null != process && process.isAlive();
    }
    
    public List<String> getOutputMessages() {
        return List.copyOf(outputMessages);
    }
    
    @Override
    public void close() {
        if (null != process) {
            process.destroy();
            try {
                if (!process.waitFor(PROCESS_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    process.waitFor(PROCESS_STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                }
                if (null != outputCollector) {
                    outputCollector.join(TimeUnit.SECONDS.toMillis(PROCESS_STOP_TIMEOUT_SECONDS));
                }
            } catch (final InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                process = null;
                outputCollector = null;
                outputMessages.clear();
            }
        }
    }
    
    private Thread startOutputCollector(final Process process, final String threadName, final List<String> outputMessages) {
        Thread result = new Thread(() -> collectOutput(process, outputMessages), threadName);
        result.setDaemon(true);
        result.start();
        return result;
    }
    
    /**
     * Prepare packaged distribution runtime layout before starting a process.
     *
     * @param distributionHome packaged distribution home
     * @param configFile runtime config file
     * @throws IOException I/O exception
     */
    public static void prepareRuntimeLayout(final Path distributionHome, final Path configFile) throws IOException {
        verifyConfigurationFile(configFile);
        verifyRuntimeLibraries(distributionHome.resolve("lib"));
        Files.createDirectories(distributionHome.resolve("data"));
        Files.createDirectories(distributionHome.resolve("logs"));
        Files.createDirectories(distributionHome.resolve("plugins"));
    }
    
    private static void verifyConfigurationFile(final Path configFile) throws IOException {
        if (Files.isRegularFile(configFile)) {
            return;
        }
        throw new IOException("MCP configuration file `" + configFile + "` does not exist.");
    }
    
    private static void verifyRuntimeLibraries(final Path libraryDirectory) throws IOException {
        if (Files.isDirectory(libraryDirectory)) {
            return;
        }
        throw new IOException("MCP runtime libraries are missing under `" + libraryDirectory + "`.");
    }
    
    private static boolean isWindows(final String osName) {
        return osName.toLowerCase(Locale.ENGLISH).startsWith(WINDOWS_OS_NAME_PREFIX);
    }
    
    private static String getClassPathSeparator(final String osName) {
        return isWindows(osName) ? WINDOWS_CLASS_PATH_SEPARATOR : UNIX_CLASS_PATH_SEPARATOR;
    }
    
    private static String resolveJavaCommand(final String osName, final String javaHome) {
        if (javaHome.isBlank()) {
            return isWindows(osName) ? JAVA_COMMAND_NAME + ".exe" : JAVA_COMMAND_NAME;
        }
        Path result = Paths.get(javaHome, "bin", isWindows(osName) ? JAVA_COMMAND_NAME + ".exe" : JAVA_COMMAND_NAME);
        return Files.exists(result) ? result.toString() : result.getFileName().toString();
    }
    
    private void collectOutput(final Process process, final List<String> outputMessages) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while (null != (line = reader.readLine())) {
                outputMessages.add(line);
            }
        } catch (final IOException ignored) {
        }
    }
}
