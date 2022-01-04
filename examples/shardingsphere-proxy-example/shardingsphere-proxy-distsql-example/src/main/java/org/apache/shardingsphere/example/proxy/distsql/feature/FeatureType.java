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

package org.apache.shardingsphere.example.proxy.distsql.feature;

import org.apache.shardingsphere.example.proxy.distsql.DistSQLExecutor;
import org.apache.shardingsphere.example.proxy.distsql.feature.encrypt.EncryptExecutor;
import org.apache.shardingsphere.example.proxy.distsql.feature.readwritesplitting.ReadWriteSplittingExecutor;
import org.apache.shardingsphere.example.proxy.distsql.feature.resource.ResourceExecutor;
import org.apache.shardingsphere.example.proxy.distsql.feature.shadow.ShadowExecutor;
import org.apache.shardingsphere.example.proxy.distsql.feature.sharding.ShardingExecutor;

public enum FeatureType {
    
    RESOURCE {
        @Override
        public DistSQLExecutor getExecutor() {
            return new ResourceExecutor();
        }
    }, SHADOW {
        @Override
        public DistSQLExecutor getExecutor() {
            return new ShadowExecutor();
        }
    }, ENCRYPT {
        @Override
        public DistSQLExecutor getExecutor() {
            return new EncryptExecutor();
        }
    }, SHARDING {
        @Override
        public DistSQLExecutor getExecutor() {
            return new ShardingExecutor();
        }
    }, READWRITE_SPLITTING {
        @Override
        public DistSQLExecutor getExecutor() {
            return new ReadWriteSplittingExecutor();
        }
    };
    
    public abstract DistSQLExecutor getExecutor();
}
