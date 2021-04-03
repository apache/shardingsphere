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

package org.apache.shardingsphere.test.integration.junit.container.adapter;

import lombok.Getter;
import org.apache.shardingsphere.test.integration.junit.annotation.XmlResource;
import org.apache.shardingsphere.test.integration.junit.container.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;
import org.apache.shardingsphere.test.integration.junit.processor.AuthenticationProcessor;
import org.apache.shardingsphere.test.integration.junit.processor.AuthenticationProcessor.Authentication;

import javax.sql.DataSource;

/**
 * ShardingSphere adapter container.
 */
public abstract class ShardingSphereAdapterContainer extends ShardingSphereContainer {
    
    @Getter
    @XmlResource(file = "/docker/{scenario}/proxy/conf/server.yaml", processor = AuthenticationProcessor.class)
    private Authentication authentication;
    
    public ShardingSphereAdapterContainer(final String dockerName, final String dockerImageName, final ParameterizedArray parameterizedArray) {
        this(dockerName, dockerImageName, false, parameterizedArray);
    }
    
    public ShardingSphereAdapterContainer(final String dockerName, final String dockerImageName, final boolean isFakeContainer, final ParameterizedArray parameterizedArray) {
        super(dockerName, dockerImageName, isFakeContainer, parameterizedArray);
    }
    
    /**
     * Get DataSource.
     *
     * @return DataSource
     */
    public abstract DataSource getDataSource();
    
}
