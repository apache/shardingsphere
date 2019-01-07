/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.opentracing.constant;

import io.opentracing.tag.StringTag;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding tags.
 *
 * @author chenqingyang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingTags {
    
    /**
     * Component of ShardingSphere's open tracing tag.
     */
    public static final String COMPONENT_NAME = "Sharding-Sphere";
    
    /**
     * Records the bind variables of SQL.
     */
    public static final StringTag DB_BIND_VARIABLES = new StringTag("db.bind_vars");
    
    /**
     * Records the connection count.
     */
    public static final StringTag CONNECTION_COUNT = new StringTag("connection.count");
}
