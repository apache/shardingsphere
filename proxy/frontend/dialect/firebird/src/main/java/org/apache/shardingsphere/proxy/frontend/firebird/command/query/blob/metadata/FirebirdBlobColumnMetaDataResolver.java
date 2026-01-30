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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.metadata;

import org.apache.shardingsphere.database.connector.firebird.metadata.data.FirebirdBlobInfoRegistry;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;

import java.sql.Types;
import java.util.OptionalInt;

public final class FirebirdBlobColumnMetaDataResolver {
    
    private final String databaseName;
    
    public FirebirdBlobColumnMetaDataResolver(final String databaseName) {
        this.databaseName = databaseName;
    }
    
    public FirebirdBlobColumnMetaData resolve(final ShardingSphereTable table, final ShardingSphereColumn column) {
        boolean blobColumn = isBlobColumn(table, column);
        Integer blobSubtype = resolveBlobSubtype(table, column, blobColumn);
        return new FirebirdBlobColumnMetaData(blobColumn, blobSubtype);
    }
    
    private Integer resolveBlobSubtype(final ShardingSphereTable table, final ShardingSphereColumn column, final boolean blobColumn) {
        if (!blobColumn || null == table || null == column) {
            return null;
        }
        OptionalInt subtype = FirebirdBlobInfoRegistry.findBlobSubtype(databaseName, table.getName(), column.getName());
        return subtype.isPresent() ? subtype.getAsInt() : null;
    }
    
    private boolean isBlobColumn(final ShardingSphereTable table, final ShardingSphereColumn column) {
        if (null == table || null == column) {
            return false;
        }
        if (FirebirdBlobInfoRegistry.isBlobColumn(databaseName, table.getName(), column.getName())) {
            return true;
        }
        return Types.BLOB == column.getDataType();
    }
}
