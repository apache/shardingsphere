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

package org.apache.shardingsphere.migration.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.api.ConsistencyCheckJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.pojo.CreateConsistencyCheckJobParameter;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.distsql.update.RALUpdater;
import org.apache.shardingsphere.migration.distsql.statement.CheckMigrationStatement;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Check migration job updater.
 */
public final class CheckMigrationJobUpdater implements RALUpdater<CheckMigrationStatement> {
    
    private static final ConsistencyCheckJobPublicAPI JOB_API = PipelineJobPublicAPIFactory.getConsistencyCheckJobPublicAPI();
    
    @Override
    public void executeUpdate(final String databaseName, final CheckMigrationStatement sqlStatement) throws SQLException {
        AlgorithmSegment typeStrategy = sqlStatement.getTypeStrategy();
        String algorithmTypeName = null == typeStrategy ? null : typeStrategy.getName();
        Properties algorithmProps = null == typeStrategy ? null : typeStrategy.getProps();
        JOB_API.createJobAndStart(new CreateConsistencyCheckJobParameter(sqlStatement.getJobId(), algorithmTypeName, algorithmProps));
    }
    
    @Override
    public String getType() {
        return CheckMigrationStatement.class.getName();
    }
}
