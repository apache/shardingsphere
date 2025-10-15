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

package org.apache.shardingsphere.encrypt.sm.algorithm.fpe.util;

/**
 * 模式
 *
 * <p>
 * 加密算法模式，是用来描述加密算法（此处特指分组密码，不包括流密码，）在加密时对明文分组的模式，它代表了不同的分组方式
 *
 * @author Looly
 * @see <a href="https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#Cipher"> Cipher章节</a>
 * @since 3.0.8
 */
public enum Mode {
    /**
     * 无模式
     */
    NONE,
    /**
     * 密码分组连接模式（Cipher Block Chaining）
     */
    CBC,
    /**
     * 密文反馈模式（Cipher Feedback）
     */
    CFB,
    /**
     * 计数器模式（A simplification of OFB）
     */
    CTR,
    /**
     * Cipher Text Stealing
     */
    CTS,
    /**
     * 电子密码本模式（Electronic CodeBook）
     */
    ECB,
    /**
     * 输出反馈模式（Output Feedback）
     */
    OFB,
    /**
     * Propagating Cipher Block
     */
    PCBC
}
