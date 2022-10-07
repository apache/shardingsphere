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

package org.apache.shardingsphere.data.pipeline.yaml.metadata;

import lombok.Data;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;

/**
 * Yaml pipeline column meta data.
 */
@Data
public final class YamlPipelineColumnMetaData implements YamlConfiguration {
    
    private int ordinalPosition;
    
    private String name;
    
    private int dataType;
    
    private String dataTypeName;
    
    private boolean nullable;
    
    private boolean primaryKey;
    
    private boolean uniqueKey;
}
