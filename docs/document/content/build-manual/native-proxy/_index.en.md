+++
title = "Native Proxy"
weight = 3
pre = "<b>10.3. </b>"
+++

Run the following command from the source repository root.

## Packaging

```bash
./mvnw -B -pl distribution/proxy-native -am \
  '-P<profiles>' \
  -Dmaven.test.skip=true package
```

## Profile Selection

Replace `<profiles>` with the required profiles, separated by commas.

| Profile                     | Generated artifact           | Notes                                                                                                                                                    |
|-----------------------------|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `release.native`            | Native distribution archive  | Requires GraalVM and a native compilation toolchain; see [Proxy Profile Selection](/en/build-manual/proxy/#profile-selection) for dependency profiles.   |
| `docker.build.native.linux` | Linux native image container | Requires Docker; outer dependency profiles are not passed into the container, so customize dependencies in the Maven command of the selected Dockerfile. |

For environment requirements, build commands, static linking options, and platform limitations, see [Building GraalVM Native Image](/en/user-manual/shardingsphere-proxy/startup/graalvm-native-image/).

## Artifacts

Native archive path:

`distribution/proxy-native/target/apache-shardingsphere-<version>-shardingsphere-proxy-bin.tar.gz`

`<version>` is the project version of the source being built.

The archive contains the native executable and configuration.
