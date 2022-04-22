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

package org.apache.shardingsphere.dbdiscovery.mysql.type;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.spi.HighlyAvailableStatus;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * Highly available status of MySQL show slave status cluster.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public final class ShowSlaveStatusHighlyAvailableStatus implements HighlyAvailableStatus {
    
    private final String primaryInstanceURL;
    
    @Override
    public void validate(final String databaseName, final Map<String, DataSource> dataSourceMap, final Properties props) {
        Preconditions.checkState(null != primaryInstanceURL, "Can not load primary data source URL in database `%s`.", databaseName);
    }
}
