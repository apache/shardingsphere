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

package org.apache.shardingsphere.infra.version;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * ShardingSphere version.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereVersion {
    
    public static final String VERSION;
    
    public static final boolean IS_SNAPSHOT;
    
    public static final String BUILD_BRANCH;
    
    public static final String BUILD_TIME;
    
    public static final String BUILD_COMMIT_ID;
    
    public static final String BUILD_COMMIT_ID_ABBREV;
    
    public static final String BUILD_COMMIT_MESSAGE_SHORT;
    
    public static final String BUILD_TAG;
    
    public static final boolean BUILD_DIRTY;
    
    static {
        VERSION = loadVersion();
        IS_SNAPSHOT = VERSION.endsWith("SNAPSHOT");
        Properties gitProps = loadGitProperties();
        BUILD_BRANCH = gitProps.getProperty("git.branch", "");
        BUILD_TIME = gitProps.getProperty("git.build.time", "");
        BUILD_COMMIT_ID = gitProps.getProperty("git.commit.id", "");
        BUILD_COMMIT_ID_ABBREV = gitProps.getProperty("git.commit.id.abbrev", "");
        BUILD_COMMIT_MESSAGE_SHORT = gitProps.getProperty("git.commit.message.short", "");
        BUILD_TAG = gitProps.getProperty("git.tags", "");
        BUILD_DIRTY = Boolean.parseBoolean(gitProps.getProperty("git.dirty", "false"));
    }
    
    /**
     * Main method.
     *
     * @param args args
     */
    public static void main(final String[] args) {
        System.out.print(getVerboseVersion());
    }
    
    private static String getVerboseVersion() {
        String result = "";
        result += String.format("ShardingSphere-%s%n", VERSION);
        if (IS_SNAPSHOT && !BUILD_COMMIT_ID.isEmpty()) {
            result += String.format("Commit ID: %s%s%n", BUILD_DIRTY ? "dirty-" : "", BUILD_COMMIT_ID);
            result += String.format("Commit Message: %s%n", BUILD_COMMIT_MESSAGE_SHORT);
            result += BUILD_TAG.isEmpty() ? String.format("Branch: %s%n", BUILD_BRANCH) : String.format("Tag: %s%n", BUILD_TAG);
            result += String.format("Build time: %s%n", BUILD_TIME);
        }
        return result;
    }
    
    private static String loadVersion() {
        Optional<String> versionFromGeneratedPropsFile = loadVersionFromGeneratedPropertiesFile();
        if (versionFromGeneratedPropsFile.isPresent()) {
            return versionFromGeneratedPropsFile.get();
        }
        Optional<String> versionFromManifest = loadVersionFromManifest();
        return versionFromManifest.orElse("");
    }
    
    private static Optional<String> loadVersionFromGeneratedPropertiesFile() {
        try (InputStream inputStream = ShardingSphereVersion.class.getResourceAsStream("/shardingsphere-version.properties")) {
            if (null == inputStream) {
                return Optional.empty();
            }
            Properties props = new Properties();
            props.load(inputStream);
            String version = props.getProperty("version");
            if (!Strings.isNullOrEmpty(version)) {
                return Optional.of(version);
            }
        } catch (final IOException ignored) {
        }
        return Optional.empty();
    }
    
    private static Optional<String> loadVersionFromManifest() {
        try {
            ClassLoader classLoader = ShardingSphereVersion.class.getClassLoader();
            Enumeration<URL> resources = classLoader.getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                try (InputStream inputStream = resource.openStream()) {
                    Manifest manifest = new Manifest(inputStream);
                    Attributes attributes = manifest.getMainAttributes();
                    String title = attributes.getValue("Implementation-Title");
                    String vendor = attributes.getValue("Implementation-Vendor");
                    if (isShardingSphereManifest(title, vendor)) {
                        String version = attributes.getValue("Implementation-Version");
                        if (!Strings.isNullOrEmpty(version)) {
                            return Optional.of(version);
                        }
                    }
                }
            }
        } catch (final IOException ignored) {
        }
        return Optional.empty();
    }
    
    private static boolean isShardingSphereManifest(final String title, final String vendor) {
        return null != title && title.toLowerCase().contains("shardingsphere") || null != vendor && vendor.toLowerCase().contains("apache");
    }
    
    private static Properties loadGitProperties() {
        try (InputStream inputStream = ShardingSphereVersion.class.getResourceAsStream("/current-git-commit.properties")) {
            if (null != inputStream) {
                Properties props = new Properties();
                props.load(inputStream);
                return props;
            }
        } catch (final IOException ignored) {
        }
        return new Properties();
    }
}
