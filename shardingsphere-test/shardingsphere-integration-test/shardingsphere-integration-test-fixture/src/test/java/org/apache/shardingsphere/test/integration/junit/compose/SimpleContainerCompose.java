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

package org.apache.shardingsphere.test.integration.junit.compose;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.junit.container.adapter.ShardingSphereAdapterContainer;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;

/**
 * Container compose.
 */
@Slf4j
public final class SimpleContainerCompose extends ContainerCompose {
    
    @Getter
    private final ShardingSphereStorageContainer storageContainer;
    
    @Getter
    private final ShardingSphereAdapterContainer adapterContainer;
    
    public SimpleContainerCompose(final String clusterName, final ParameterizedArray parameterizedArray) {
        super(clusterName, parameterizedArray);
        this.storageContainer = createStorageContainer();
        this.adapterContainer = createAdapterContainer();
        adapterContainer.dependsOn(storageContainer);
    }
    
}
