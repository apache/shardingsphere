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

package org.apache.shardingsphere.data.pipeline.core.datanode;

import com.google.common.base.Splitter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.InvalidDataNodesFormatException;

import java.util.List;

/**
 * Data node utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataNodeUtils {
    
    /**
     * Format data node as string with schema.
     *
     * @param dataNode data node
     * @return formatted data node
     */
    public static String formatWithSchema(final DataNode dataNode) {
        return dataNode.getDataSourceName() + (null != dataNode.getSchemaName() ? "." + dataNode.getSchemaName() : "") + "." + dataNode.getTableName();
    }
    
    /**
     * Parse data node from text.
     *
     * @param text data node text
     * @return data node
     * @throws InvalidDataNodesFormatException invalid data nodes format exception
     */
    public static DataNode parseWithSchema(final String text) {
        List<String> segments = Splitter.on(".").splitToList(text);
        boolean hasSchema = 3 == segments.size();
        if (!(2 == segments.size() || hasSchema)) {
            throw new InvalidDataNodesFormatException(text);
        }
        DataNode result = new DataNode(segments.get(0), segments.get(segments.size() - 1));
        if (hasSchema) {
            result.setSchemaName(segments.get(1));
        }
        return result;
    }
}
