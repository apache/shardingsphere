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

package org.apache.shardingsphere.example.proxy.distsql.hint;

import org.apache.shardingsphere.example.proxy.distsql.DistSQLExecutor;
import org.apache.shardingsphere.example.proxy.distsql.hint.impl.AddShardingExecutor;
import org.apache.shardingsphere.example.proxy.distsql.hint.impl.SetShardingExecutor;
import org.apache.shardingsphere.example.proxy.distsql.hint.impl.SetReadwriteSplittingExecutor;

public enum HintType {
    
    SET_SHARDING {
        @Override
        public String getConfigPath() {
            return "/client/datasource-config.yaml";
        }
    
        @Override
        public DistSQLExecutor getExecutor() {
            return new SetShardingExecutor();
        }
    }, ADD_SHARDING {
        @Override
        public String getConfigPath() {
            return "/client/datasource-config.yaml";
        }
        
        @Override
        public DistSQLExecutor getExecutor() {
            return new AddShardingExecutor();
        }
    }, SET_READWRITE_SPLITTING {
        @Override
        public String getConfigPath() {
            return "/client/datasource-config.yaml";
        }
        
        @Override
        public DistSQLExecutor getExecutor() {
            return new SetReadwriteSplittingExecutor();
        }
    };
    
    public abstract String getConfigPath();
    
    public abstract DistSQLExecutor getExecutor();
}
