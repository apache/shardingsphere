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

package org.apache.shardingsphere.test.integration.framework.container.atomic.adapter;

import lombok.SneakyThrows;
import org.apache.shardingsphere.test.integration.framework.container.atomic.ShardingSphereContainer;
import org.apache.shardingsphere.test.integration.framework.param.model.ParameterizedArray;

import javax.sql.DataSource;

/**
 * Adapter container.
 */
public abstract class AdapterContainer extends ShardingSphereContainer {
    
    public AdapterContainer(final String dockerName, final String dockerImageName, final ParameterizedArray parameterizedArray) {
        this(dockerName, dockerImageName, false, parameterizedArray);
    }
    
    @SneakyThrows
    public AdapterContainer(final String name, final String dockerImageName, final boolean isFakedContainer, final ParameterizedArray parameterizedArray) {
        super(name, dockerImageName, isFakedContainer, parameterizedArray);
    }
    
    /**
     * Get data source.
     *
     * @param serverLists server list
     * @return data source
     */
    public abstract DataSource getDataSource(String serverLists);
    
    /**
     * Get governance data source.
     *
     * @param serverLists server list
     * @return data source
     */
    public abstract DataSource getDataSourceForReader(String serverLists);
}
