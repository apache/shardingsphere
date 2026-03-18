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

package org.apache.shardingsphere.infra.instance.metadata.jdbc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.instance.util.IpUtils;

/**
 * JDBC instance meta data.
 */
@RequiredArgsConstructor
@Getter
public final class JDBCInstanceMetaData implements InstanceMetaData {
    
    private final String id;
    
    private final String ip;
    
    private final String version;
    
    private final String databaseName;
    
    public JDBCInstanceMetaData(final String id, final String databaseName) {
        this(id, IpUtils.getIp(), ShardingSphereVersion.VERSION, databaseName);
    }
    
    @Override
    public InstanceType getType() {
        return InstanceType.JDBC;
    }
    
    @Override
    public String getAttributes() {
        return ip;
    }
}
