/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.transport.mysql.constant;

/**
 * Sharding-Proxy's information.
 * 
 * @author zhangliang 
 */
public interface ServerInfo {
    
    /**
     * Protocol version is always 0x0A.
     */
    int PROTOCOL_VERSION = 0x0A;
    
    /**
     * Server version.
     */
    String SERVER_VERSION = "5.5.59-Sharding-Proxy 2.1.0";
    
    /**
     * Charset code 0x21 is utf8_general_ci.
     */
    int CHARSET = 0x21;
}
