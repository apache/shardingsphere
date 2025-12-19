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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.infra.exception.generic.FileIOException;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedClusterInfo;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedMetaData;
import org.apache.shardingsphere.proxy.backend.util.MetaDataImportExecutor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Import meta data executor.
 */
public final class ImportMetaDataExecutor implements DistSQLUpdateExecutor<ImportMetaDataStatement> {
    
    @Override
    public void executeUpdate(final ImportMetaDataStatement sqlStatement, final ContextManager contextManager) {
        String jsonMetaDataConfig = sqlStatement.getFilePath().isPresent() ? getMetaDataFromFile(sqlStatement) : getMetaDataFromConsole(sqlStatement);
        ExportedClusterInfo exportedClusterInfo = JsonUtils.fromJsonString(jsonMetaDataConfig, ExportedClusterInfo.class);
        ExportedMetaData exportedMetaData = exportedClusterInfo.getMetaData();
        new MetaDataImportExecutor(contextManager).importClusterConfigurations(exportedMetaData);
    }
    
    private String getMetaDataFromFile(final ImportMetaDataStatement sqlStatement) {
        if (!sqlStatement.getFilePath().isPresent()) {
            return "";
        }
        File file = new File(sqlStatement.getFilePath().get());
        try {
            return FileUtils.readFileToString(file, Charset.defaultCharset());
        } catch (final IOException ignore) {
            throw new FileIOException(file);
        }
    }
    
    private String getMetaDataFromConsole(final ImportMetaDataStatement sqlStatement) {
        return new String(Base64.decodeBase64(sqlStatement.getMetaDataValue()));
    }
    
    @Override
    public Class<ImportMetaDataStatement> getType() {
        return ImportMetaDataStatement.class;
    }
}
