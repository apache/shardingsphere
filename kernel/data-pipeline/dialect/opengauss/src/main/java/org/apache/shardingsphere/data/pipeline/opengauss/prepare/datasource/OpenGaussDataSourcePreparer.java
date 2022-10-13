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

package org.apache.shardingsphere.data.pipeline.opengauss.prepare.datasource;

import com.google.common.base.Splitter;
import org.apache.curator.shaded.com.google.common.base.Strings;
import org.apache.shardingsphere.data.pipeline.api.config.CreateTableConfiguration.CreateTableEntry;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.AbstractDataSourcePreparer;
import org.apache.shardingsphere.data.pipeline.core.prepare.datasource.PrepareTargetTablesParameter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * Data source preparer for openGauss.
 */
public final class OpenGaussDataSourcePreparer extends AbstractDataSourcePreparer {
    
    @Override
    public void prepareTargetTables(final PrepareTargetTablesParameter parameter) throws SQLException {
        PipelineDataSourceManager dataSourceManager = parameter.getDataSourceManager();
        for (CreateTableEntry each : parameter.getCreateTableConfig().getCreateTableEntries()) {
            String createTargetTableSQL = getCreateTargetTableSQL(each, dataSourceManager, parameter.getSqlParserEngine());
            try (Connection targetConnection = getCachedDataSource(dataSourceManager, each.getTargetDataSourceConfig()).getConnection()) {
                for (String sql : Splitter.on(";").trimResults().splitToList(createTargetTableSQL).stream().filter(cs -> !Strings.isNullOrEmpty(cs)).collect(Collectors.toList())) {
                    executeTargetTableSQL(targetConnection, sql);
                }
            }
        }
    }
    
    @Override
    public String getType() {
        return "openGauss";
    }
}
