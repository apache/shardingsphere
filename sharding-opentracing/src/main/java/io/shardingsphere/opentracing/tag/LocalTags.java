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

package io.shardingsphere.opentracing.tag;

/**
 * @author chenqingyang
 */

import io.opentracing.tag.StringTag;

public final class LocalTags {

    public static final String COMPONENT_NAME = "SHARDING-SPHERE";

    /**
     * DB_BIND_VARIABLES records the bind variables of sql statement.
     */
    public static final StringTag DB_BIND_VARIABLES = new StringTag("db.bind_vars");

    private LocalTags() {

    }

}
