package io.shardingjdbc.orchestration.reg.etcd.internal.stub;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.7.0)",
    comments = "Source: rpc.proto")
public final class AuthGrpc {

  public static final String SERVICE_NAME = "etcdserverpb.Auth";
  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableResponse> METHOD_AUTH_ENABLE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "AuthEnable"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("AuthEnable"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableResponse> METHOD_AUTH_DISABLE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "AuthDisable"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("AuthDisable"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateResponse> METHOD_AUTHENTICATE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "Authenticate"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("Authenticate"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddResponse> METHOD_USER_ADD =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "UserAdd"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("UserAdd"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetResponse> METHOD_USER_GET =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "UserGet"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("UserGet"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListResponse> METHOD_USER_LIST =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "UserList"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("UserList"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteResponse> METHOD_USER_DELETE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "UserDelete"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("UserDelete"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordResponse> METHOD_USER_CHANGE_PASSWORD =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "UserChangePassword"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("UserChangePassword"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleResponse> METHOD_USER_GRANT_ROLE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "UserGrantRole"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("UserGrantRole"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleResponse> METHOD_USER_REVOKE_ROLE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "UserRevokeRole"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("UserRevokeRole"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddResponse> METHOD_ROLE_ADD =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "RoleAdd"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("RoleAdd"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetResponse> METHOD_ROLE_GET =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "RoleGet"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("RoleGet"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListResponse> METHOD_ROLE_LIST =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "RoleList"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("RoleList"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteResponse> METHOD_ROLE_DELETE =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "RoleDelete"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("RoleDelete"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionResponse> METHOD_ROLE_GRANT_PERMISSION =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "RoleGrantPermission"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("RoleGrantPermission"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionRequest,
      io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionResponse> METHOD_ROLE_REVOKE_PERMISSION =
      io.grpc.MethodDescriptor.<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionRequest, io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionResponse>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "etcdserverpb.Auth", "RoleRevokePermission"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionRequest.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionResponse.getDefaultInstance()))
          .setSchemaDescriptor(new AuthMethodDescriptorSupplier("RoleRevokePermission"))
          .build();
  private static final int METHODID_AUTH_ENABLE = 0;
  private static final int METHODID_AUTH_DISABLE = 1;
  private static final int METHODID_AUTHENTICATE = 2;
  private static final int METHODID_USER_ADD = 3;
  private static final int METHODID_USER_GET = 4;
  private static final int METHODID_USER_LIST = 5;
  private static final int METHODID_USER_DELETE = 6;
  private static final int METHODID_USER_CHANGE_PASSWORD = 7;
  private static final int METHODID_USER_GRANT_ROLE = 8;
  private static final int METHODID_USER_REVOKE_ROLE = 9;
  private static final int METHODID_ROLE_ADD = 10;
  private static final int METHODID_ROLE_GET = 11;
  private static final int METHODID_ROLE_LIST = 12;
  private static final int METHODID_ROLE_DELETE = 13;
  private static final int METHODID_ROLE_GRANT_PERMISSION = 14;
  private static final int METHODID_ROLE_REVOKE_PERMISSION = 15;
  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;
  private AuthGrpc() {}

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static AuthStub newStub(io.grpc.Channel channel) {
    return new AuthStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static AuthBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new AuthBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static AuthFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new AuthFutureStub(channel);
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (AuthGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new AuthFileDescriptorSupplier())
              .addMethod(METHOD_AUTH_ENABLE)
              .addMethod(METHOD_AUTH_DISABLE)
              .addMethod(METHOD_AUTHENTICATE)
              .addMethod(METHOD_USER_ADD)
              .addMethod(METHOD_USER_GET)
              .addMethod(METHOD_USER_LIST)
              .addMethod(METHOD_USER_DELETE)
              .addMethod(METHOD_USER_CHANGE_PASSWORD)
              .addMethod(METHOD_USER_GRANT_ROLE)
              .addMethod(METHOD_USER_REVOKE_ROLE)
              .addMethod(METHOD_ROLE_ADD)
              .addMethod(METHOD_ROLE_GET)
              .addMethod(METHOD_ROLE_LIST)
              .addMethod(METHOD_ROLE_DELETE)
              .addMethod(METHOD_ROLE_GRANT_PERMISSION)
              .addMethod(METHOD_ROLE_REVOKE_PERMISSION)
              .build();
        }
      }
    }
    return result;
  }

  /**
   */
  public static abstract class AuthImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * AuthEnable enables authentication.
     * </pre>
     */
    public void authEnable(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableRequest request,
                           io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_AUTH_ENABLE, responseObserver);
    }

    /**
     * <pre>
     * AuthDisable disables authentication.
     * </pre>
     */
    public void authDisable(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableRequest request,
                            io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_AUTH_DISABLE, responseObserver);
    }

    /**
     * <pre>
     * Authenticate processes an authenticate request.
     * </pre>
     */
    public void authenticate(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateRequest request,
                             io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_AUTHENTICATE, responseObserver);
    }

    /**
     * <pre>
     * UserAdd adds a new user.
     * </pre>
     */
    public void userAdd(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddRequest request,
                        io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_USER_ADD, responseObserver);
    }

    /**
     * <pre>
     * UserGet gets detailed user information.
     * </pre>
     */
    public void userGet(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetRequest request,
                        io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_USER_GET, responseObserver);
    }

    /**
     * <pre>
     * UserList gets a list of all users.
     * </pre>
     */
    public void userList(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListRequest request,
                         io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_USER_LIST, responseObserver);
    }

    /**
     * <pre>
     * UserDelete deletes a specified user.
     * </pre>
     */
    public void userDelete(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteRequest request,
                           io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_USER_DELETE, responseObserver);
    }

    /**
     * <pre>
     * UserChangePassword changes the password of a specified user.
     * </pre>
     */
    public void userChangePassword(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordRequest request,
                                   io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_USER_CHANGE_PASSWORD, responseObserver);
    }

    /**
     * <pre>
     * UserGrant grants a role to a specified user.
     * </pre>
     */
    public void userGrantRole(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleRequest request,
                              io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_USER_GRANT_ROLE, responseObserver);
    }

    /**
     * <pre>
     * UserRevokeRole revokes a role of specified user.
     * </pre>
     */
    public void userRevokeRole(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleRequest request,
                               io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_USER_REVOKE_ROLE, responseObserver);
    }

    /**
     * <pre>
     * RoleAdd adds a new role.
     * </pre>
     */
    public void roleAdd(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddRequest request,
                        io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ROLE_ADD, responseObserver);
    }

    /**
     * <pre>
     * RoleGet gets detailed role information.
     * </pre>
     */
    public void roleGet(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetRequest request,
                        io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ROLE_GET, responseObserver);
    }

    /**
     * <pre>
     * RoleList gets lists of all roles.
     * </pre>
     */
    public void roleList(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListRequest request,
                         io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ROLE_LIST, responseObserver);
    }

    /**
     * <pre>
     * RoleDelete deletes a specified role.
     * </pre>
     */
    public void roleDelete(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteRequest request,
                           io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ROLE_DELETE, responseObserver);
    }

    /**
     * <pre>
     * RoleGrantPermission grants a permission of a specified key or range to a specified role.
     * </pre>
     */
    public void roleGrantPermission(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionRequest request,
                                    io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ROLE_GRANT_PERMISSION, responseObserver);
    }

    /**
     * <pre>
     * RoleRevokePermission revokes a key or range permission of a specified role.
     * </pre>
     */
    public void roleRevokePermission(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionRequest request,
                                     io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionResponse> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ROLE_REVOKE_PERMISSION, responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_AUTH_ENABLE,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableResponse>(
                  this, METHODID_AUTH_ENABLE)))
          .addMethod(
            METHOD_AUTH_DISABLE,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableResponse>(
                  this, METHODID_AUTH_DISABLE)))
          .addMethod(
            METHOD_AUTHENTICATE,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateResponse>(
                  this, METHODID_AUTHENTICATE)))
          .addMethod(
            METHOD_USER_ADD,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddResponse>(
                  this, METHODID_USER_ADD)))
          .addMethod(
            METHOD_USER_GET,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetResponse>(
                  this, METHODID_USER_GET)))
          .addMethod(
            METHOD_USER_LIST,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListResponse>(
                  this, METHODID_USER_LIST)))
          .addMethod(
            METHOD_USER_DELETE,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteResponse>(
                  this, METHODID_USER_DELETE)))
          .addMethod(
            METHOD_USER_CHANGE_PASSWORD,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordResponse>(
                  this, METHODID_USER_CHANGE_PASSWORD)))
          .addMethod(
            METHOD_USER_GRANT_ROLE,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleResponse>(
                  this, METHODID_USER_GRANT_ROLE)))
          .addMethod(
            METHOD_USER_REVOKE_ROLE,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleResponse>(
                  this, METHODID_USER_REVOKE_ROLE)))
          .addMethod(
            METHOD_ROLE_ADD,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddResponse>(
                  this, METHODID_ROLE_ADD)))
          .addMethod(
            METHOD_ROLE_GET,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetResponse>(
                  this, METHODID_ROLE_GET)))
          .addMethod(
            METHOD_ROLE_LIST,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListResponse>(
                  this, METHODID_ROLE_LIST)))
          .addMethod(
            METHOD_ROLE_DELETE,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteResponse>(
                  this, METHODID_ROLE_DELETE)))
          .addMethod(
            METHOD_ROLE_GRANT_PERMISSION,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionResponse>(
                  this, METHODID_ROLE_GRANT_PERMISSION)))
          .addMethod(
            METHOD_ROLE_REVOKE_PERMISSION,
            asyncUnaryCall(
              new MethodHandlers<
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionRequest,
                io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionResponse>(
                  this, METHODID_ROLE_REVOKE_PERMISSION)))
          .build();
    }
  }

  /**
   */
  public static final class AuthStub extends io.grpc.stub.AbstractStub<AuthStub> {
    private AuthStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AuthStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected AuthStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AuthStub(channel, callOptions);
    }

    /**
     * <pre>
     * AuthEnable enables authentication.
     * </pre>
     */
    public void authEnable(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableRequest request,
                           io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_AUTH_ENABLE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * AuthDisable disables authentication.
     * </pre>
     */
    public void authDisable(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableRequest request,
                            io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_AUTH_DISABLE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Authenticate processes an authenticate request.
     * </pre>
     */
    public void authenticate(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateRequest request,
                             io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_AUTHENTICATE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * UserAdd adds a new user.
     * </pre>
     */
    public void userAdd(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddRequest request,
                        io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_USER_ADD, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * UserGet gets detailed user information.
     * </pre>
     */
    public void userGet(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetRequest request,
                        io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_USER_GET, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * UserList gets a list of all users.
     * </pre>
     */
    public void userList(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListRequest request,
                         io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_USER_LIST, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * UserDelete deletes a specified user.
     * </pre>
     */
    public void userDelete(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteRequest request,
                           io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_USER_DELETE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * UserChangePassword changes the password of a specified user.
     * </pre>
     */
    public void userChangePassword(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordRequest request,
                                   io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_USER_CHANGE_PASSWORD, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * UserGrant grants a role to a specified user.
     * </pre>
     */
    public void userGrantRole(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleRequest request,
                              io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_USER_GRANT_ROLE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * UserRevokeRole revokes a role of specified user.
     * </pre>
     */
    public void userRevokeRole(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleRequest request,
                               io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_USER_REVOKE_ROLE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * RoleAdd adds a new role.
     * </pre>
     */
    public void roleAdd(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddRequest request,
                        io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ROLE_ADD, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * RoleGet gets detailed role information.
     * </pre>
     */
    public void roleGet(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetRequest request,
                        io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ROLE_GET, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * RoleList gets lists of all roles.
     * </pre>
     */
    public void roleList(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListRequest request,
                         io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ROLE_LIST, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * RoleDelete deletes a specified role.
     * </pre>
     */
    public void roleDelete(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteRequest request,
                           io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ROLE_DELETE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * RoleGrantPermission grants a permission of a specified key or range to a specified role.
     * </pre>
     */
    public void roleGrantPermission(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionRequest request,
                                    io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ROLE_GRANT_PERMISSION, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * RoleRevokePermission revokes a key or range permission of a specified role.
     * </pre>
     */
    public void roleRevokePermission(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionRequest request,
                                     io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ROLE_REVOKE_PERMISSION, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class AuthBlockingStub extends io.grpc.stub.AbstractStub<AuthBlockingStub> {
    private AuthBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AuthBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected AuthBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AuthBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * AuthEnable enables authentication.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableResponse authEnable(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_AUTH_ENABLE, getCallOptions(), request);
    }

    /**
     * <pre>
     * AuthDisable disables authentication.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableResponse authDisable(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_AUTH_DISABLE, getCallOptions(), request);
    }

    /**
     * <pre>
     * Authenticate processes an authenticate request.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateResponse authenticate(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_AUTHENTICATE, getCallOptions(), request);
    }

    /**
     * <pre>
     * UserAdd adds a new user.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddResponse userAdd(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_USER_ADD, getCallOptions(), request);
    }

    /**
     * <pre>
     * UserGet gets detailed user information.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetResponse userGet(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_USER_GET, getCallOptions(), request);
    }

    /**
     * <pre>
     * UserList gets a list of all users.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListResponse userList(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_USER_LIST, getCallOptions(), request);
    }

    /**
     * <pre>
     * UserDelete deletes a specified user.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteResponse userDelete(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_USER_DELETE, getCallOptions(), request);
    }

    /**
     * <pre>
     * UserChangePassword changes the password of a specified user.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordResponse userChangePassword(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_USER_CHANGE_PASSWORD, getCallOptions(), request);
    }

    /**
     * <pre>
     * UserGrant grants a role to a specified user.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleResponse userGrantRole(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_USER_GRANT_ROLE, getCallOptions(), request);
    }

    /**
     * <pre>
     * UserRevokeRole revokes a role of specified user.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleResponse userRevokeRole(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_USER_REVOKE_ROLE, getCallOptions(), request);
    }

    /**
     * <pre>
     * RoleAdd adds a new role.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddResponse roleAdd(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ROLE_ADD, getCallOptions(), request);
    }

    /**
     * <pre>
     * RoleGet gets detailed role information.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetResponse roleGet(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ROLE_GET, getCallOptions(), request);
    }

    /**
     * <pre>
     * RoleList gets lists of all roles.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListResponse roleList(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ROLE_LIST, getCallOptions(), request);
    }

    /**
     * <pre>
     * RoleDelete deletes a specified role.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteResponse roleDelete(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ROLE_DELETE, getCallOptions(), request);
    }

    /**
     * <pre>
     * RoleGrantPermission grants a permission of a specified key or range to a specified role.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionResponse roleGrantPermission(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ROLE_GRANT_PERMISSION, getCallOptions(), request);
    }

    /**
     * <pre>
     * RoleRevokePermission revokes a key or range permission of a specified role.
     * </pre>
     */
    public io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionResponse roleRevokePermission(io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionRequest request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ROLE_REVOKE_PERMISSION, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class AuthFutureStub extends io.grpc.stub.AbstractStub<AuthFutureStub> {
    private AuthFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AuthFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected AuthFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AuthFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * AuthEnable enables authentication.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableResponse> authEnable(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_AUTH_ENABLE, getCallOptions()), request);
    }

    /**
     * <pre>
     * AuthDisable disables authentication.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableResponse> authDisable(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_AUTH_DISABLE, getCallOptions()), request);
    }

    /**
     * <pre>
     * Authenticate processes an authenticate request.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateResponse> authenticate(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_AUTHENTICATE, getCallOptions()), request);
    }

    /**
     * <pre>
     * UserAdd adds a new user.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddResponse> userAdd(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_USER_ADD, getCallOptions()), request);
    }

    /**
     * <pre>
     * UserGet gets detailed user information.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetResponse> userGet(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_USER_GET, getCallOptions()), request);
    }

    /**
     * <pre>
     * UserList gets a list of all users.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListResponse> userList(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_USER_LIST, getCallOptions()), request);
    }

    /**
     * <pre>
     * UserDelete deletes a specified user.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteResponse> userDelete(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_USER_DELETE, getCallOptions()), request);
    }

    /**
     * <pre>
     * UserChangePassword changes the password of a specified user.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordResponse> userChangePassword(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_USER_CHANGE_PASSWORD, getCallOptions()), request);
    }

    /**
     * <pre>
     * UserGrant grants a role to a specified user.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleResponse> userGrantRole(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_USER_GRANT_ROLE, getCallOptions()), request);
    }

    /**
     * <pre>
     * UserRevokeRole revokes a role of specified user.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleResponse> userRevokeRole(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_USER_REVOKE_ROLE, getCallOptions()), request);
    }

    /**
     * <pre>
     * RoleAdd adds a new role.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddResponse> roleAdd(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ROLE_ADD, getCallOptions()), request);
    }

    /**
     * <pre>
     * RoleGet gets detailed role information.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetResponse> roleGet(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ROLE_GET, getCallOptions()), request);
    }

    /**
     * <pre>
     * RoleList gets lists of all roles.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListResponse> roleList(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ROLE_LIST, getCallOptions()), request);
    }

    /**
     * <pre>
     * RoleDelete deletes a specified role.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteResponse> roleDelete(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ROLE_DELETE, getCallOptions()), request);
    }

    /**
     * <pre>
     * RoleGrantPermission grants a permission of a specified key or range to a specified role.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionResponse> roleGrantPermission(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ROLE_GRANT_PERMISSION, getCallOptions()), request);
    }

    /**
     * <pre>
     * RoleRevokePermission revokes a key or range permission of a specified role.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionResponse> roleRevokePermission(
        io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionRequest request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ROLE_REVOKE_PERMISSION, getCallOptions()), request);
    }
  }

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AuthImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(AuthImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_AUTH_ENABLE:
          serviceImpl.authEnable((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthEnableResponse>) responseObserver);
          break;
        case METHODID_AUTH_DISABLE:
          serviceImpl.authDisable((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthDisableResponse>) responseObserver);
          break;
        case METHODID_AUTHENTICATE:
          serviceImpl.authenticate((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthenticateResponse>) responseObserver);
          break;
        case METHODID_USER_ADD:
          serviceImpl.userAdd((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserAddResponse>) responseObserver);
          break;
        case METHODID_USER_GET:
          serviceImpl.userGet((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGetResponse>) responseObserver);
          break;
        case METHODID_USER_LIST:
          serviceImpl.userList((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserListResponse>) responseObserver);
          break;
        case METHODID_USER_DELETE:
          serviceImpl.userDelete((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserDeleteResponse>) responseObserver);
          break;
        case METHODID_USER_CHANGE_PASSWORD:
          serviceImpl.userChangePassword((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserChangePasswordResponse>) responseObserver);
          break;
        case METHODID_USER_GRANT_ROLE:
          serviceImpl.userGrantRole((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserGrantRoleResponse>) responseObserver);
          break;
        case METHODID_USER_REVOKE_ROLE:
          serviceImpl.userRevokeRole((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthUserRevokeRoleResponse>) responseObserver);
          break;
        case METHODID_ROLE_ADD:
          serviceImpl.roleAdd((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleAddResponse>) responseObserver);
          break;
        case METHODID_ROLE_GET:
          serviceImpl.roleGet((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGetResponse>) responseObserver);
          break;
        case METHODID_ROLE_LIST:
          serviceImpl.roleList((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleListResponse>) responseObserver);
          break;
        case METHODID_ROLE_DELETE:
          serviceImpl.roleDelete((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleDeleteResponse>) responseObserver);
          break;
        case METHODID_ROLE_GRANT_PERMISSION:
          serviceImpl.roleGrantPermission((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleGrantPermissionResponse>) responseObserver);
          break;
        case METHODID_ROLE_REVOKE_PERMISSION:
          serviceImpl.roleRevokePermission((io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionRequest) request,
              (io.grpc.stub.StreamObserver<io.shardingjdbc.orchestration.reg.etcd.internal.stub.AuthRoleRevokePermissionResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class AuthBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    AuthBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.shardingjdbc.orchestration.reg.etcd.internal.stub.EtcdProto.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Auth");
    }
  }

  private static final class AuthFileDescriptorSupplier
      extends AuthBaseDescriptorSupplier {
    AuthFileDescriptorSupplier() {}
  }

  private static final class AuthMethodDescriptorSupplier
      extends AuthBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    AuthMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }
}
