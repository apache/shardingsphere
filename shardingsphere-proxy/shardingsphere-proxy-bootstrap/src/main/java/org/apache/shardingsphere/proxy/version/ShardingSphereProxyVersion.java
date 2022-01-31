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

package org.apache.shardingsphere.proxy.version;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;

/**
 * ShardingSphere-Proxy version.
 */
public final class ShardingSphereProxyVersion {
    
    /**
     * Get version.
     * 
     * @return version
     */
    public static String getVersion() {
        String result = ShardingSphereVersion.VERSION;
        if (!ShardingSphereVersion.IS_SNAPSHOT || Strings.isNullOrEmpty(ShardingSphereVersion.BUILD_GIT_COMMIT_ID_ABBREV)) {
            return result;
        }
        result += ShardingSphereVersion.BUILD_GIT_DIRTY ? "-dirty" : "";
        result += "-" + ShardingSphereVersion.BUILD_GIT_COMMIT_ID_ABBREV;
        return result;
    }
}
