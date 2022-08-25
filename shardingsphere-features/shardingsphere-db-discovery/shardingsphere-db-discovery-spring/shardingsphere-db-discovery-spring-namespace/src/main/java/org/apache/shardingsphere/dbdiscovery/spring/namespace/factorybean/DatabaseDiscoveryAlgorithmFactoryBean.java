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

package org.apache.shardingsphere.dbdiscovery.spring.namespace.factorybean;

import org.apache.shardingsphere.dbdiscovery.factory.DatabaseDiscoveryProviderAlgorithmFactory;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.spring.namespace.factorybean.ShardingSphereAlgorithmFactoryBean;

import java.util.Properties;

/**
 * Database discovery algorithm factory bean.
 */
public final class DatabaseDiscoveryAlgorithmFactoryBean extends ShardingSphereAlgorithmFactoryBean<DatabaseDiscoveryProviderAlgorithm> {
    
    public DatabaseDiscoveryAlgorithmFactoryBean(final String type, final Properties props) {
        super(type, props, DatabaseDiscoveryProviderAlgorithm.class);
    }
    
    @Override
    public DatabaseDiscoveryProviderAlgorithm getObject() {
        return DatabaseDiscoveryProviderAlgorithmFactory.newInstance(new AlgorithmConfiguration(getType(), getProps()));
    }
}
