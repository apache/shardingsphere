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

package org.apache.shardingsphere.dbdiscovery.opengauss.replication;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.spi.status.RoleSeparatedHighlyAvailableStatus;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * Highly available status of openGauss normal replication cluster.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public final class OpenGaussNormalReplicationHighlyAvailableStatus implements RoleSeparatedHighlyAvailableStatus {
    
    private final boolean primary;
    
    @Override
    public void validate(final String databaseName, final Map<String, DataSource> dataSourceMap, final Properties props) {
    }
}
