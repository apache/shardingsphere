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

package org.apache.shardingsphere.sql.parser.sql.common.constant;

/**
 * Join type enum.
 */
public enum JoinType {
    MYSQL_INNER_JOIN("INNER"),
    MYSQL_STRAIGHT_JOIN("STRAIGHT"),
    MYSQL_LEFT_JOIN("LEFT"),
    MYSQL_RIGHT_JOIN("RIGHT"),
    MYSQL_NATURAL_INNER_JOIN("NATURAL_INNER"),
    MYSQL_NATURAL_LEFT_JOIN("NATURAL_LEFT"),
    MYSQL_NATURAL_RIGHT_JOIN("NATURAL_RIGHT");

    private final String joinType;

    JoinType(final String joinType) {
        this.joinType = joinType;
    }

    /**
     * Get join type.
     *
     * @return table join type
     */
    public String getJoinType() {
        return joinType;
    }
}
