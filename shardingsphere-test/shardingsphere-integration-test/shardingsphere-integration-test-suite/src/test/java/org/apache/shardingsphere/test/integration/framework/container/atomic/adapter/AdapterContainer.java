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

import org.apache.shardingsphere.test.integration.framework.container.atomic.AtomicContainer;

import javax.sql.DataSource;

/**
 * Adapter container.
 */
public abstract class AdapterContainer extends AtomicContainer {
    
    public AdapterContainer(final String dockerName, final String dockerImageName) {
        this(dockerName, dockerImageName, false);
    }
    
    public AdapterContainer(final String name, final String dockerImageName, final boolean isFakedContainer) {
        super(name, dockerImageName, isFakedContainer);
    }
    
    /**
     * Get operation data source.
     *
     * @param serverLists server lists
     * @return operation data source
     */
    public abstract DataSource getOperationDataSource(String serverLists);
    
    /**
     * Get verification data source.
     *
     * @param serverLists server lists
     * @return verification data source
     */
    public abstract DataSource getVerificationDataSource(String serverLists);
}
