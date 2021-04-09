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

import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.test.integration.junit.container.storage.ShardingSphereStorageContainer;
import org.apache.shardingsphere.test.integration.junit.param.model.ParameterizedArray;

import java.util.Objects;

/**
 * H2 container.
 */
public final class H2Container extends ShardingSphereStorageContainer {
    
    public H2Container(final ParameterizedArray parameterizedArray) {
        super("h2-embedded", "h2:fake", new H2DatabaseType(), true, parameterizedArray);
    }
    
    @Override
    public boolean isHealthy() {
        return true;
    }
    
    @Override
    protected String getUrl(final String dataSourceName) {
        return String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", Objects.isNull(dataSourceName) ? "test_db" : dataSourceName);
    }
    
    @Override
    protected int getPort() {
        return 0;
    }
    
    @Override
    protected String getUsername() {
        return "sa";
    }
    
    @Override
    protected String getPassword() {
        return "";
    }
    
}
