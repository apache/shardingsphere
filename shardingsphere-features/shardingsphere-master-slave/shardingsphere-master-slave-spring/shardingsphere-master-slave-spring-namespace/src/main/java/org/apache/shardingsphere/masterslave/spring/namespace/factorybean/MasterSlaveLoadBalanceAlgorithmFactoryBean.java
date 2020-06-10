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

package org.apache.shardingsphere.masterslave.spring.namespace.factorybean;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.masterslave.spi.MasterSlaveLoadBalanceAlgorithm;
import org.springframework.beans.factory.FactoryBean;

import java.util.Properties;

/**
 * Master-slave load balance algorithm factory bean.
 */
@RequiredArgsConstructor
@Getter
public final class MasterSlaveLoadBalanceAlgorithmFactoryBean implements FactoryBean<MasterSlaveLoadBalanceAlgorithm> {
    
    static {
        ShardingSphereServiceLoader.register(MasterSlaveLoadBalanceAlgorithm.class);
    }
    
    private final String id;
    
    private final String type;
    
    private final Properties properties;
    
    @Override
    public MasterSlaveLoadBalanceAlgorithm getObject() {
        return TypedSPIRegistry.getRegisteredService(MasterSlaveLoadBalanceAlgorithm.class, type, properties);
    }
    
    @Override
    public Class<?> getObjectType() {
        return MasterSlaveLoadBalanceAlgorithm.class;
    }
    
    @Override
    public boolean isSingleton() {
        return true;
    }
}
