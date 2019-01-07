/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.transport.mysql.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding-Proxy's information.
 * 
 * @author zhangliang 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerInfo {
    
    /**
     * Protocol version is always 0x0A.
     */
    public static final int PROTOCOL_VERSION = 0x0A;
    
    /**
     * Server version.
     */
    public static final String SERVER_VERSION = "5.6.0-Sharding-Proxy 3.1.0";
    
    /**
     * Charset code 0x21 is utf8_general_ci.
     */
    public static final int CHARSET = 0x21;
}
