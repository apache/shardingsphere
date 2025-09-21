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

package org.apache.shardingsphere.test.e2e.env.runtime.type;

import lombok.Getter;
import org.apache.shardingsphere.test.e2e.env.container.constants.StorageContainerConstants;

import java.util.Properties;

/**
 * Native storage environment.
 */
@Getter
public final class NativeStorageEnvironment {
    
    private final String host;
    
    private final int port;
    
    private final String user;
    
    private final String password;
    
    public NativeStorageEnvironment(final Properties props) {
        host = props.getProperty("it.native.storage.host", "127.0.0.1");
        port = Integer.parseInt(props.getProperty("it.native.storage.port", "0"));
        user = props.getProperty("it.native.storage.username", StorageContainerConstants.OPERATION_USER);
        password = props.getProperty("it.native.storage.password", StorageContainerConstants.OPERATION_PASSWORD);
    }
}
