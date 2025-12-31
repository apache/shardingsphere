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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.position.type;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.InventoryDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.position.InventoryDataRecordPositionCreator;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.PrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.UniqueKeyIngestPosition;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Unique key inventory data record position creator.
 */
public final class UniqueKeyInventoryDataRecordPositionCreator implements InventoryDataRecordPositionCreator {
    
    @Override
    public IngestPosition create(final InventoryDumperContext dumperContext, final ResultSet resultSet) throws SQLException {
        return UniqueKeyIngestPosition.newInstance(Range.closed(resultSet.getObject(dumperContext.getUniqueKeyColumns().get(0).getName()),
                ((PrimaryKeyIngestPosition<?>) dumperContext.getCommonContext().getPosition()).getUpperBound()));
    }
}
