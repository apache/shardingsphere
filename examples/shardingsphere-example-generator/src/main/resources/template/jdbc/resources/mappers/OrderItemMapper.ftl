<?xml version="1.0" encoding="UTF-8" ?>
<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
<#assign package = feature?replace('-', '')?replace(',', '.') />

<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.repository.OrderItemRepository">
    <resultMap id="baseResultMap" type="org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.entity.OrderItem">
        <result column="order_item_id" property="orderItemId" jdbcType="INTEGER"/>
        <result column="order_id" property="orderId" jdbcType="INTEGER"/>
        <result column="user_id" property="userId" jdbcType="INTEGER"/>
        <result column="phone" property="phone" jdbcType="VARCHAR"/>
        <result column="status" property="status" jdbcType="VARCHAR"/>
    </resultMap>
    
    <update id="createTableIfNotExists">
        CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT AUTO_INCREMENT, order_id BIGINT, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50) , PRIMARY KEY (order_item_id));
    </update>
    
    <update id="truncateTable">
        TRUNCATE TABLE t_order_item;
    </update>
    
    <update id="dropTable">
        DROP TABLE IF EXISTS t_order_item;
    </update>
 <#if feature?contains("shadow")>
    
    <update id="createTableIfNotExistsShadow">
        CREATE TABLE IF NOT EXISTS t_order_item (order_item_id BIGINT AUTO_INCREMENT, order_id BIGINT, user_id INT NOT NULL, phone VARCHAR(50), status VARCHAR(50) , PRIMARY KEY (order_item_id));
    </update>
    
    <update id="truncateTableShadow">
        TRUNCATE TABLE t_order_item;
    </update>
    
    <update id="dropTableShadow">
        DROP TABLE IF EXISTS t_order_item;
    </update>
 </#if>
    
    <insert id="insert" useGeneratedKeys="true" keyProperty="orderItemId">
        INSERT INTO t_order_item (order_id, user_id, phone, status) VALUES (${r"#{orderId,jdbcType=INTEGER}"}, ${r"#{userId,jdbcType=INTEGER}"}, ${r"#{phone,jdbcType=VARCHAR}"}, ${r"#{status,jdbcType=VARCHAR}"});
    </insert>
    
    <delete id="delete">
        DELETE FROM t_order_item WHERE order_id = ${r"#{orderId,jdbcType=INTEGER}"};
    </delete>
    
    <select id="selectAll" resultMap="baseResultMap">
        SELECT * from t_order_item
    </select>
</mapper>
