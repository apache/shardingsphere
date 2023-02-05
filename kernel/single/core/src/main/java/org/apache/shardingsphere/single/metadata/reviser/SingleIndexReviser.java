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

package org.apache.shardingsphere.single.metadata.reviser;

import org.apache.shardingsphere.infra.metadata.database.schema.decorator.reviser.index.IndexReviser;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtil;

/**
 * Single index reviser.
 */
public final class SingleIndexReviser implements IndexReviser {
    
    @Override
    public IndexMetaData revise(final TableMetaData tableMetaData, final IndexMetaData originalMetaData) {
        return new IndexMetaData(IndexMetaDataUtil.getLogicIndexName(originalMetaData.getName(), tableMetaData.getName()));
    }
}
