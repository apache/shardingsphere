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
    MYSQL_INNER_JOIN("INNER", "MySQL"),
    MYSQL_STRAIGHT_JOIN("STRAIGHT", "MySQL"),
    MYSQL_LEFT_JOIN("LEFT", "MySQL"),
    MYSQL_RIGHT_JOIN("RIGHT", "MySQL"),
    MYSQL_NATURAL_INNER_JOIN("NATURAL_INNER", "MySQL"),
    MYSQL_NATURAL_LEFT_JOIN("NATURAL_LEFT", "MySQL"),
    MYSQL_NATURAL_RIGHT_JOIN("NATURAL_RIGHT", "MySQL");

    private final String joinType;
    private final String databaseType;

    JoinType(final String joinType, final String databaseType) {
        this.joinType = joinType;
        this.databaseType = databaseType;
    }

    /**
     * Get joinType.
     * @return joinType.
     */
    public String getJoinType() {
        return joinType;
    }

    /**
     * Get databaseType.
     * @return databaseType.
     */
    public String getDatabaseType() {
        return databaseType;
    }
}
