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
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.datanode.InvalidDataNodeFormatException;

import java.util.List;

/**
 * Data node utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataNodeUtils {
    
    /**
     * Parse data node from text.
     *
     * @param text data node text
     * @return data node
     * @throws InvalidDataNodeFormatException invalid data nodes format exception
     */
    public static DataNode parseWithSchema(final String text) {
        List<String> segments = Splitter.on(".").splitToList(text);
        ShardingSpherePreconditions.checkState(2 == segments.size() || 3 == segments.size(), () -> new InvalidDataNodeFormatException(text));
        DataNode result = new DataNode(segments.get(0), segments.get(segments.size() - 1));
        if (3 == segments.size()) {
            result.setSchemaName(segments.get(1));
        }
        return result;
    }
}
