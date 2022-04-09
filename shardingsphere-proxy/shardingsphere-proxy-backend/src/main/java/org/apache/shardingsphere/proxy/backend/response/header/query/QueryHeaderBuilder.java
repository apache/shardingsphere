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

package org.apache.shardingsphere.proxy.backend.response.header.query;

import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.spi.type.required.RequiredSPI;
import org.apache.shardingsphere.spi.type.typed.StatelessTypedSPI;

import java.sql.SQLException;

/**
 * Query header builder.
 */
public interface QueryHeaderBuilder extends StatelessTypedSPI, RequiredSPI {
    
    /**
     * Build query header.
     * 
     * @param queryResultMetaData query result meta data
     * @param metaData ShardingSphere meta data
     * @param columnName column name
     * @param columnLabel column label
     * @param columnIndex column index
     * @param dataNodeContainedRule data node contained rule
     * @return query header
     * @throws SQLException SQL exception
     */
    QueryHeader build(QueryResultMetaData queryResultMetaData, ShardingSphereMetaData metaData,
                      String columnName, String columnLabel, int columnIndex, LazyInitializer<DataNodeContainedRule> dataNodeContainedRule) throws SQLException;
}
