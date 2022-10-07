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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

/**
 * Mppdb decoding Gson related class.
 */
@Setter
@Getter
public final class MppTableData {
    
    @SerializedName("table_name")
    private String tableName;
    
    @SerializedName("op_type")
    private String opType;
    
    @SerializedName("columns_name")
    private String[] columnsName;
    
    @SerializedName("columns_type")
    private String[] columnsType;
    
    @SerializedName("columns_val")
    private String[] columnsVal;
    
    @SerializedName("old_keys_name")
    private String[] oldKeysName;
    
    @SerializedName("old_keys_type")
    private String[] oldKeysType;
    
    @SerializedName("old_keys_val")
    private String[] oldKeysVal;
}
