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

package org.apache.shardingsphere.mode.repository.cluster.nacos.constant;

/**
 * Jdbc constant.
 */
public class JdbcConstants {
    
    private static final String TABLE_NAME = "NACOS_INTERNAL_LOCK";
    
    private static final String COLUMN = "LOCK_NAME";
    
    private static final String INDEX = "LOCK_NAME_INDEX";
    
    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + COLUMN + " VARCHAR(255) NOT NULL, UNIQUE INDEX " + INDEX + "(" + COLUMN + "))";
    
    public static final String INSERT_NACOS_INTERNAL_LOCK = "INSERT INTO " + TABLE_NAME + "(" + COLUMN + ") VALUES (?)";
    
    public static final String DELETE_NACOS_INTERNAL_LOCK = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN + " = ?";
}
