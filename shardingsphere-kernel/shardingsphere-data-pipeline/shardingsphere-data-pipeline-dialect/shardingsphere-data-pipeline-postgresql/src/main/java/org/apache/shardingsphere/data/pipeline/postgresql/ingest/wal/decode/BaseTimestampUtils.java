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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * logical replication decoding plugin interface.
 */
public interface BaseTimestampUtils {
    
    /**
     * Get time.
     *
     * @param cal the cal
     * @param input the input time of string
     * @return Time the time
     * @throws SQLException the exp
     */
    Time toTime(Calendar cal, String input) throws SQLException;
    
    /**
     * Get timestamp.
     *
     * @param cal the cal
     * @param input the input timestamp of string
     * @return Timestamp the timestamp
     * @throws SQLException the exp
     */
    Timestamp toTimestamp(Calendar cal, String input) throws SQLException;
}
