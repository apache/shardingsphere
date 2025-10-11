# Use Eclipse Temurin JDK as the base image
FROM eclipse-temurin:23-jdk

# Metadata
LABEL org.opencontainers.image.authors="ShardingSphere dev@shardingsphere.apache.org"

# Define build arguments
ARG DIRECTORY_NAME
ARG JAR_NAME

# Set working directory
WORKDIR /usr/agent

# Copy ShardingSphere Agent files
ADD target/${DIRECTORY_NAME} /usr/agent/

# Rename and ensure the agent JAR is properly placed
RUN if [ -f "/usr/agent/${JAR_NAME}" ]; then \
        mv /usr/agent/${JAR_NAME} /usr/agent/shardingsphere-agent.jar; \
    else \
        echo "Error: ShardingSphere Agent JAR not found!"; exit 1; \
    fi

# Environment variable for Java Agent
ENV JAVA_TOOL_OPTIONS="-javaagent:/usr/agent/shardingsphere-agent.jar"

# Default command for running applications with the agent
CMD ["java", "-jar", "app.jar"]
