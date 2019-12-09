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

package org.apache.shardingsphere.shardingscaling.core.web.entity;

/**
 * Http response message.
 *
 * @author ssxlulu
 */
public final class ResponseMessage {

    public static final String START_SUCCESS = "Strat job success!";

    public static final String GET_PROGRESS_SUCCESS = "Get progress of the job success!";

    public static final String GET_PROGRESS_ERROR = "Get progress of the job failed!";

    public static final String STOP_SUCCESS = "Stop job success!";

    public static final String BAD_REQUEST = "Not support request!";

    public static final String INTERNAL_SERVER_ERROR = "Internal server error!";

}
