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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Properties;

/**
 * Build info of ShardingSphere.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereVersion {
    
    static {
        Properties properties = new Properties();
        try {
            properties.load(ShardingSphereVersion.class.getResourceAsStream("/current-git-commit.properties"));
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
        BUILD_BRANCH = properties.getProperty("git.branch");
        BUILD_HOST = properties.getProperty("git.build.host");
        BUILD_TIME = properties.getProperty("git.build.time");
        BUILD_GIT_USER_EMAIL = properties.getProperty("git.build.user.email");
        BUILD_GIT_USER_NAME = properties.getProperty("git.build.user.name");
        BUILD_MAVEN_PROJECT_VERSION = properties.getProperty("git.build.version");
        BUILD_GIT_COMMIT_ID = properties.getProperty("git.commit.id");
        BUILD_GIT_COMMIT_ID_SHORT = properties.getProperty("git.commit.id.abbrev");
        BUILD_GIT_COMMIT_MESSAGE = properties.getProperty("git.commit.message");
        BUILD_GIT_COMMIT_MESSAGE_SHORT = properties.getProperty("git.commit.message.short");
        BUILD_GIT_TAG = properties.getProperty("git.tags");
        BUILD_GIT_DIRTY = Boolean.parseBoolean(properties.getProperty("git.dirty"));
    }
    
    public static final String BUILD_BRANCH;
    
    public static final String BUILD_HOST;
    
    public static final String BUILD_TIME;
    
    public static final String BUILD_GIT_USER_EMAIL;
    
    public static final String BUILD_GIT_USER_NAME;
    
    public static final String BUILD_MAVEN_PROJECT_VERSION;
    
    public static final String BUILD_GIT_COMMIT_ID;
    
    public static final String BUILD_GIT_COMMIT_ID_SHORT;
    
    public static final String BUILD_GIT_COMMIT_MESSAGE;
    
    public static final String BUILD_GIT_COMMIT_MESSAGE_SHORT;
    
    public static final String BUILD_GIT_TAG;
    
    public static final boolean BUILD_GIT_DIRTY;
    
    /**
     * Print version info of ShardingSphere to stdout.
     *
     * @param args args
     */
    public static void main(final String[] args) {
        System.out.println(getVerboseVersion());
    }
    
    private static String getVerboseVersion() {
        String result = "";
        result += String.format("ShardingSphere-%s\n", BUILD_MAVEN_PROJECT_VERSION);
        result += String.format("Commit ID: %s%s\n", BUILD_GIT_DIRTY ? "dirty-" : "", BUILD_GIT_COMMIT_ID);
        result += String.format("Commit Message: %s\n", BUILD_GIT_COMMIT_MESSAGE_SHORT);
        result += String.format("Branch: %s\n", BUILD_BRANCH);
        result += String.format("Build time: %s\n", BUILD_TIME);
        return result;
    }
}
