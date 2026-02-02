<?xml version="1.0" encoding="UTF-8"?>
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

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.apache.shardingsphere.example</groupId>
    <artifactId>${feature?replace(',', '-')}--${framework}--${mode}--${transaction}</artifactId>
    <version>${shardingsphereVersion}</version>
    <name>${r'${project.artifactId}'}</name>
    <#assign repository = repository!'JDBC'>
    
    <dependencies>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-jdbc</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-infra-data-source-pool-hikari</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-database-connector-mysql</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-authority-simple</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-parser-sql-engine-mysql</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-infra-url-classpath</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
    <#if feature?contains("sharding")>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-sharding-core</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-broadcast-core</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
    </#if>
    <#if feature?contains("readwrite-splitting")>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-readwrite-splitting-core</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
    </#if>
    <#if feature?contains("encrypt")>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-encrypt-core</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
    </#if>
    <#if feature?contains("shadow")>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-shadow-core</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-sql-parser-api</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-parser-sql-engine-core</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
    </#if>
    <#if feature?contains("mask")>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-mask-core</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
    </#if>
    <#if mode=="cluster-zookeeper">
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-cluster-mode-repository-zookeeper</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
    </#if>
    <#if mode?contains("standalone") && repository == "JDBC">
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-standalone-mode-repository-jdbc</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
    </#if>
    <#if transaction?contains("xa")>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-transaction-xa-core</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
    </#if>
    <#if transaction=="xa-atomikos">
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-transaction-xa-atomikos</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
    </#if>
    <#if transaction=="xa-narayana">
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-transaction-xa-narayana</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
        <dependency>
            <groupId>org.jboss.narayana.jta</groupId>
            <artifactId>jta</artifactId>
            <version>5.12.4.Final</version>
        </dependency>
        <dependency>
            <groupId>org.jboss.narayana.jts</groupId>
            <artifactId>narayana-jts-integration</artifactId>
            <version>5.12.4.Final</version>
        </dependency>
        <dependency>
            <groupId>org.jboss</groupId>
            <artifactId>jboss-transaction-spi</artifactId>
            <version>7.6.0.Final</version>
        </dependency>
        <dependency>
            <groupId>org.jboss.logging</groupId>
            <artifactId>jboss-logging</artifactId>
            <version>3.2.1.Final</version>
        </dependency>
    <#elseif transaction=="base-seata">
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
            <version>${r'${project.version}'}</version>
        </dependency>
        <dependency>
            <groupId>io.seata</groupId>
            <artifactId>seata-all</artifactId>
            <version>1.5.2</version>
        </dependency>
    </#if>
    <#if framework?contains("jpa")>
        <dependency>
            <groupId>org.hibernate.javax.persistence</groupId>
            <artifactId>hibernate-jpa-2.1-api</artifactId>
            <version>1.0.0.Final</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>5.4.24.Final</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate</groupId>
            <artifactId>hibernate-entitymanager</artifactId>
            <version>5.4.24.Final</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-orm</artifactId>
            <version>5.2.15.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
            <version>5.2.15.RELEASE</version>
        </dependency>
    </#if>
    <#if framework=="spring-boot-starter-jdbc">
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
            <version>2.2.0.RELEASE</version>
            <exclusions>
                <exclusion>
                    <artifactId>snakeyaml</artifactId>
                    <groupId>org.yaml</groupId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <artifactId>snakeyaml</artifactId>
            <groupId>org.yaml</groupId>
            <version>1.33</version>
        </dependency>
    <#elseif framework=="spring-boot-starter-mybatis">
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.1.3</version>
            <exclusions>
                <exclusion>
                    <artifactId>snakeyaml</artifactId>
                    <groupId>org.yaml</groupId>
                </exclusion>
            </exclusions>
        </dependency>
    <#elseif framework=="spring-namespace-jdbc">
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
            <version>5.2.15.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>5.2.15.RELEASE</version>
        </dependency>
    <#elseif framework=="spring-namespace-mybatis">
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.5.9</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis-spring</artifactId>
            <version>2.0.5</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-orm</artifactId>
            <version>5.2.15.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
            <version>5.2.15.RELEASE</version>
        </dependency>
    <#elseif framework?contains("spring-boot-starter")>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot</artifactId>
            <version>2.2.0.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
            <version>2.2.0.RELEASE</version>
        </dependency>
    </#if>
        
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.30</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>3.4.2</version>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <version>8.3.0</version>
        </dependency>
        
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.36</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.3.16</version>
        </dependency>
    </dependencies>

    <profiles>
        <profile>
            <id>example-generator</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.codehaus.mojo</groupId>
                        <artifactId>exec-maven-plugin</artifactId>
                        <version>3.0.0</version>
                        <executions>
                            <execution>
                                <phase>test</phase>
                                <goals>
                                    <goal>java</goal>
                                </goals>
                                <configuration>
                                    <#assign package = feature?replace('-', '')?replace(',', '.') />
                                    <mainClass>org.apache.shardingsphere.example.${package}.${framework?replace('-', '.')}.ExampleMain</mainClass>
                                </configuration>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
    
    <build>
        <plugins>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.1</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
