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

package org.apache.shardingsphere.encrypt.metadata.reviser.index;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.infra.metadata.database.schema.reviser.index.IndexReviser;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

/**
 * Encrypt index reviser.
 */
@RequiredArgsConstructor
public final class EncryptIndexReviser implements IndexReviser<EncryptRule> {
    
    private final EncryptTable encryptTable;
    
    @Override
    public Optional<IndexMetaData> revise(final String tableName, final IndexMetaData originalMetaData, final EncryptRule rule) {
        if (originalMetaData.getColumns().isEmpty()) {
            return Optional.empty();
        }
        Collection<String> columns = new LinkedHashSet<>(originalMetaData.getColumns().size(), 1F);
        for (String each : originalMetaData.getColumns()) {
            if (encryptTable.isCipherColumn(each)) {
                columns.add(encryptTable.getLogicColumnByCipherColumn(each));
            } else if (encryptTable.isAssistedQueryColumn(each)) {
                columns.add(encryptTable.getLogicColumnByAssistedQueryColumn(each));
            } else {
                columns.add(each);
            }
        }
        IndexMetaData result = new IndexMetaData(originalMetaData.getName(), columns);
        result.setUnique(originalMetaData.isUnique());
        return Optional.of(result);
    }
}
