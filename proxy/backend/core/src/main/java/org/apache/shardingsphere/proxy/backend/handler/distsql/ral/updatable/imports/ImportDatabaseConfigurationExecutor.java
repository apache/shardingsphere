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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.imports;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.infra.exception.generic.FileIOException;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.util.MetaDataImportExecutor;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * Import database configuration executor.
 */
public final class ImportDatabaseConfigurationExecutor implements DistSQLUpdateExecutor<ImportDatabaseConfigurationStatement> {
    
    @Override
    public void executeUpdate(final ImportDatabaseConfigurationStatement sqlStatement, final ContextManager contextManager) {
        YamlProxyDatabaseConfiguration yamlConfig = getYamlProxyDatabaseConfiguration(sqlStatement);
        new MetaDataImportExecutor(contextManager).importDatabaseConfigurations(Collections.singletonList(yamlConfig));
    }
    
    private YamlProxyDatabaseConfiguration getYamlProxyDatabaseConfiguration(final ImportDatabaseConfigurationStatement sqlStatement) {
        File file = new File(sqlStatement.getFilePath());
        try {
            return YamlEngine.unmarshal(file, YamlProxyDatabaseConfiguration.class);
        } catch (final IOException ignore) {
            throw new FileIOException(file);
        }
    }
    
    @Override
    public Class<ImportDatabaseConfigurationStatement> getType() {
        return ImportDatabaseConfigurationStatement.class;
    }
}
