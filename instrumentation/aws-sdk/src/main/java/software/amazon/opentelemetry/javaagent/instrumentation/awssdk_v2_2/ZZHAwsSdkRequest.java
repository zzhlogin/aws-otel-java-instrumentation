/*
 * Copyright Amazon.com, Inc. or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2;

import static software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsSdkRequestType.BEDROCK;
import static software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsSdkRequestType.BEDROCKAGENTOPERATION;
import static software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsSdkRequestType.BEDROCKAGENTRUNTIMEOPERATION;
import static software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsSdkRequestType.BEDROCKDATASOURCEOPERATION;
import static software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsSdkRequestType.BEDROCKKNOWLEDGEBASEOPERATION;
import static software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsSdkRequestType.BEDROCKRUNTIME;
import static software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsSdkRequestType.LAMBDA;
import static software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsSdkRequestType.SECRETSMANAGER;
import static software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsSdkRequestType.SNS;
import static software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsSdkRequestType.STEPFUNCTION;
import static software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.FieldMapping.request;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import software.amazon.awssdk.core.SdkRequest;

// We match the actual name in the AWS SDK for better consistency with it and possible future
// autogeneration.
enum ZZHAwsSdkRequest {
  BedrockRequest(BEDROCK, "BedrockRequest"),
  BedrockAgentRuntimeRequest(BEDROCKAGENTRUNTIMEOPERATION, "BedrockAgentRuntimeRequest"),
  BedrockRuntimeRequest(BEDROCKRUNTIME, "BedrockRuntimeRequest"),
  // BedrockAgent API based requests. We only support operations that are related to
  // Agent/DataSources/KnowledgeBases
  // resources and the request/response context contains the resource ID.
  BedrockCreateAgentActionGroupRequest(BEDROCKAGENTOPERATION, "CreateAgentActionGroupRequest"),
  BedrockCreateAgentAliasRequest(BEDROCKAGENTOPERATION, "CreateAgentAliasRequest"),
  BedrockDeleteAgentActionGroupRequest(BEDROCKAGENTOPERATION, "DeleteAgentActionGroupRequest"),
  BedrockDeleteAgentAliasRequest(BEDROCKAGENTOPERATION, "DeleteAgentAliasRequest"),
  BedrockDeleteAgentVersionRequest(BEDROCKAGENTOPERATION, "DeleteAgentVersionRequest"),
  BedrockGetAgentActionGroupRequest(BEDROCKAGENTOPERATION, "GetAgentActionGroupRequest"),
  BedrockGetAgentAliasRequest(BEDROCKAGENTOPERATION, "GetAgentAliasRequest"),
  BedrockGetAgentRequest(BEDROCKAGENTOPERATION, "GetAgentRequest"),
  BedrockGetAgentVersionRequest(BEDROCKAGENTOPERATION, "GetAgentVersionRequest"),
  BedrockListAgentActionGroupsRequest(BEDROCKAGENTOPERATION, "ListAgentActionGroupsRequest"),
  BedrockListAgentAliasesRequest(BEDROCKAGENTOPERATION, "ListAgentAliasesRequest"),
  BedrockListAgentKnowledgeBasesRequest(BEDROCKAGENTOPERATION, "ListAgentKnowledgeBasesRequest"),
  BedrocListAgentVersionsRequest(BEDROCKAGENTOPERATION, "ListAgentVersionsRequest"),
  BedrockPrepareAgentRequest(BEDROCKAGENTOPERATION, "PrepareAgentRequest"),
  BedrockUpdateAgentActionGroupRequest(BEDROCKAGENTOPERATION, "UpdateAgentActionGroupRequest"),
  BedrockUpdateAgentAliasRequest(BEDROCKAGENTOPERATION, "UpdateAgentAliasRequest"),
  BedrockUpdateAgentRequest(BEDROCKAGENTOPERATION, "UpdateAgentRequest"),
  BedrockBedrockAgentRequest(BEDROCKAGENTOPERATION, "BedrockAgentRequest"),
  BedrockDeleteDataSourceRequest(BEDROCKDATASOURCEOPERATION, "DeleteDataSourceRequest"),
  BedrockGetDataSourceRequest(BEDROCKDATASOURCEOPERATION, "GetDataSourceRequest"),
  BedrockUpdateDataSourceRequest(BEDROCKDATASOURCEOPERATION, "UpdateDataSourceRequest"),
  BedrocAssociateAgentKnowledgeBaseRequest(
      BEDROCKKNOWLEDGEBASEOPERATION, "AssociateAgentKnowledgeBaseRequest"),
  BedrockCreateDataSourceRequest(BEDROCKKNOWLEDGEBASEOPERATION, "CreateDataSourceRequest"),
  BedrockDeleteKnowledgeBaseRequest(BEDROCKKNOWLEDGEBASEOPERATION, "DeleteKnowledgeBaseRequest"),
  BedrockDisassociateAgentKnowledgeBaseRequest(
      BEDROCKKNOWLEDGEBASEOPERATION, "DisassociateAgentKnowledgeBaseRequest"),
  BedrockGetAgentKnowledgeBaseRequest(
      BEDROCKKNOWLEDGEBASEOPERATION, "GetAgentKnowledgeBaseRequest"),
  BedrockGetKnowledgeBaseRequest(BEDROCKKNOWLEDGEBASEOPERATION, "GetKnowledgeBaseRequest"),
  BedrockListDataSourcesRequest(BEDROCKKNOWLEDGEBASEOPERATION, "ListDataSourcesRequest"),
  BedrockUpdateAgentKnowledgeBaseRequest(
      BEDROCKKNOWLEDGEBASEOPERATION, "UpdateAgentKnowledgeBaseRequest"),
  SfnRequest(STEPFUNCTION, "SfnRequest"),
  SnsRequest(SNS, "SnsRequest"),
  SecretsManagerRequest(SECRETSMANAGER, "SecretsManagerRequest"),
  LambdaRequest(LAMBDA, "LambdaRequest");

  private final AwsSdkRequestType type;
  private final String requestClass;
  private final Map<FieldMapping.Type, List<FieldMapping>> fields;

  ZZHAwsSdkRequest(AwsSdkRequestType type, String requestClass, FieldMapping... fields) {
    this.type = type;
    this.requestClass = requestClass;
    this.fields = Collections.unmodifiableMap(FieldMapping.groupByType(fields));
  }

  @Nullable
  static ZZHAwsSdkRequest ofSdkRequest(SdkRequest request) {
    // try request type
    ZZHAwsSdkRequest result = ofType(request.getClass().getSimpleName());
    // try parent - generic
    if (result == null) {
      result = ofType(request.getClass().getSuperclass().getSimpleName());
    }
    return result;
  }

  private static ZZHAwsSdkRequest ofType(String typeName) {
    for (ZZHAwsSdkRequest type : values()) {
      if (type.requestClass.equals(typeName)) {
        return type;
      }
    }
    return null;
  }

  List<FieldMapping> fields(FieldMapping.Type type) {
    return fields.get(type);
  }

  AwsSdkRequestType type() {
    return type;
  }
}
