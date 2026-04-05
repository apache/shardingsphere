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

package org.apache.shardingsphere.test.e2e.mcp;

final class LLME2EAssertionReport {
    
    private final boolean success;
    
    private final String failureType;
    
    private final String message;
    
    private LLME2EAssertionReport(final boolean success, final String failureType, final String message) {
        this.success = success;
        this.failureType = failureType;
        this.message = message;
    }
    
    static LLME2EAssertionReport success(final String message) {
        return new LLME2EAssertionReport(true, "", message);
    }
    
    boolean success() {
        return success;
    }
    
    static LLME2EAssertionReport failure(final String failureType, final String message) {
        return new LLME2EAssertionReport(false, failureType, message);
    }
    
    String failureType() {
        return failureType;
    }
    
    String message() {
        return message;
    }
}
