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

package org.apache.shardingsphere.test.integration.junit.container.storage.impl;

import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;

/**
 * Native storage container.
 */
public final class NativeStorageContainer extends ShardingSphereStorageContainer {
    
    public NativeStorageContainer(final ParameterizedArray parameterizedArray) {
        super("native", "native-storage", null, true, parameterizedArray);
    }
    
    @Override
    protected String getUrl(final String dataSourceName) {
        return null;
    }
    
    @Override
    protected int getPort() {
        return 0;
    }
    
    @Override
    protected String getUsername() {
        return null;
    }
    
    @Override
    protected String getPassword() {
        return null;
    }
}
