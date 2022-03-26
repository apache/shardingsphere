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
<mapper namespace="org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.repository.OrderRepository">
    <resultMap id="baseResultMap" type="org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.entity.Order">
        <result column="order_id" property="orderId" jdbcType="BIGINT"/>
        <result column="order_type" property="orderType" jdbcType="BIGINT"/>
        <result column="user_id" property="userId" jdbcType="INTEGER"/>
        <result column="address_id" property="addressId" jdbcType="BIGINT"/>
        <result column="status" property="status" jdbcType="VARCHAR"/>
    </resultMap>

    <update id="createTableIfNotExists">
        CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id));
    </update>
    
    <update id="truncateTable">
        TRUNCATE TABLE t_order;
    </update>
    
    <update id="dropTable">
        DROP TABLE IF EXISTS t_order;
    </update>
 <#if feature?contains("shadow")>
    
    <update id="createTableIfNotExistsShadow">
        CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT AUTO_INCREMENT, order_type INT(11), user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id)); /*shadow:true,foo:bar*/
    </update>
    
    <update id="truncateTableShadow">
        TRUNCATE TABLE t_order; /*shadow:true,foo:bar*/
    </update>
    
    <update id="dropTableShadow">
        DROP TABLE IF EXISTS t_order; /*shadow:true,foo:bar*/
    </update>

     <select id="selectShadowOrder" resultMap="baseResultMap">
         SELECT * FROM t_order WHERE order_type=1;
     </select>

     <delete id="deleteShadow">
         DELETE FROM t_order WHERE order_id = ${r"#{orderId,jdbcType=INTEGER}"} AND order_type=1;
     </delete>
 </#if>
    
    <insert id="insert" useGeneratedKeys="true" keyProperty="orderId">
        INSERT INTO t_order (user_id, order_type, address_id, status) VALUES (${r"#{userId,jdbcType=INTEGER}"}, ${r"#{orderType,jdbcType=INTEGER}"}, ${r"#{addressId,jdbcType=BIGINT}"}, ${r"#{status,jdbcType=VARCHAR}"});
    </insert>
    
    <delete id="delete">
        DELETE FROM t_order WHERE order_id = ${r"#{orderId,jdbcType=INTEGER}"};
    </delete>
    
    <select id="selectAll" resultMap="baseResultMap">
        SELECT * FROM t_order;
    </select>
</mapper>
