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

package org.apache.shardingsphere.infra.autogen.version;

import java.io.IOException;
import java.util.Properties;

/**
 * Build info of ShardingSphere. 
 * Values prefixed with `BUILD` will be empty if building from source codes without .git directory.
 */
public final class ShardingSphereVersion {
    
    public static final String VERSION = "${project.version}";
    
    static {
        Properties info = new Properties();
        try {
            info.load(ShardingSphereVersion.class.getResourceAsStream("/current-git-commit.properties"));
        } catch (final Exception ignored) {
        }
        IS_SNAPSHOT = VERSION.endsWith("SNAPSHOT");
        BUILD_BRANCH = info.getProperty("git.branch", "");
        BUILD_TIME = info.getProperty("git.build.time", "");
        BUILD_GIT_COMMIT_ID = info.getProperty("git.commit.id", "");
        BUILD_GIT_COMMIT_ID_ABBREV = info.getProperty("git.commit.id.abbrev", "");
        BUILD_GIT_COMMIT_MESSAGE_SHORT = info.getProperty("git.commit.message.short", "");
        BUILD_GIT_TAG = info.getProperty("git.tags", "");
        BUILD_GIT_DIRTY = Boolean.parseBoolean(info.getProperty("git.dirty"));
    }
    
    public static final boolean IS_SNAPSHOT;
    
    public static final String BUILD_BRANCH;
    
    public static final String BUILD_TIME;
    
    public static final String BUILD_GIT_COMMIT_ID;
    
    public static final String BUILD_GIT_COMMIT_ID_ABBREV;
    
    public static final String BUILD_GIT_COMMIT_MESSAGE_SHORT;
    
    public static final String BUILD_GIT_TAG;
    
    public static final boolean BUILD_GIT_DIRTY;
    
    private ShardingSphereVersion() {
    }
    
    /**
     * Print version info of ShardingSphere to stdout.
     *
     * @param args args
     */
    public static void main(final String[] args) {
        System.out.print(getVerboseVersion());
    }
    
    private static String getVerboseVersion() {
        String result = "";
        result += String.format("ShardingSphere-%s%n", VERSION);
        if (IS_SNAPSHOT && !BUILD_GIT_COMMIT_ID.isEmpty()) {
            result += String.format("Commit ID: %s%s%n", BUILD_GIT_DIRTY ? "dirty-" : "", BUILD_GIT_COMMIT_ID);
            result += String.format("Commit Message: %s%n", BUILD_GIT_COMMIT_MESSAGE_SHORT);
            result += BUILD_GIT_TAG.isEmpty() ? String.format("Branch: %s%n", BUILD_BRANCH) : String.format("Tag: %s%n", BUILD_GIT_TAG);
            result += String.format("Build time: %s%n", BUILD_TIME);
        }
        return result;
    }
}
