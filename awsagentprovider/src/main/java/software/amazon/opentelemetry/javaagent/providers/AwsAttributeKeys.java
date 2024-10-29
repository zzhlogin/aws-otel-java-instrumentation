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

package software.amazon.opentelemetry.javaagent.providers;

import io.opentelemetry.api.common.AttributeKey;

/** Utility class holding attribute keys with special meaning to AWS components */
final class AwsAttributeKeys {

  private AwsAttributeKeys() {}

  static final AttributeKey<String> AWS_SPAN_KIND = AttributeKey.stringKey("aws.span.kind");

  static final AttributeKey<String> AWS_LOCAL_SERVICE = AttributeKey.stringKey("aws.local.service");

  static final AttributeKey<String> AWS_LOCAL_OPERATION =
      AttributeKey.stringKey("aws.local.operation");

  static final AttributeKey<String> AWS_REMOTE_SERVICE =
      AttributeKey.stringKey("aws.remote.service");

  static final AttributeKey<String> AWS_REMOTE_OPERATION =
      AttributeKey.stringKey("aws.remote.operation");

  static final AttributeKey<String> AWS_REMOTE_RESOURCE_IDENTIFIER =
      AttributeKey.stringKey("aws.remote.resource.identifier");

  static final AttributeKey<String> AWS_CLOUDFORMATION_PRIMARY_IDENTIFIER =
      AttributeKey.stringKey("aws.remote.resource.cfn.primary.identifier");

  static final AttributeKey<String> AWS_REMOTE_RESOURCE_TYPE =
      AttributeKey.stringKey("aws.remote.resource.type");

  static final AttributeKey<String> AWS_REMOTE_DB_USER =
      AttributeKey.stringKey("aws.remote.db.user");

  static final AttributeKey<String> AWS_SDK_DESCENDANT =
      AttributeKey.stringKey("aws.sdk.descendant");

  static final AttributeKey<String> AWS_CONSUMER_PARENT_SPAN_KIND =
      AttributeKey.stringKey("aws.consumer.parent.span.kind");

  static final AttributeKey<String> AWS_STATE_MACHINE_ARN =
      AttributeKey.stringKey("aws.stepfunctions.state_machine.arn");

  static final AttributeKey<String> AWS_STEP_FUNCTIONS_ACTIVITY_ARN =
      AttributeKey.stringKey("aws.stepfunctions.activity.arn");

  static final AttributeKey<String> AWS_SNS_TOPIC_ARN = AttributeKey.stringKey("aws.sns.topic.arn");

  static final AttributeKey<String> AWS_SECRET_ARN =
      AttributeKey.stringKey("aws.secretsmanager.secret.arn");

  static final AttributeKey<String> AWS_LAMBDA_ARN =
      AttributeKey.stringKey("aws.lambda.function.arn");

  static final AttributeKey<String> AWS_LAMBDA_RESOURCE_ID =
      AttributeKey.stringKey("aws.lambda.resource_mapping.id");

  // use the same AWS Resource attribute name defined by OTel java auto-instr for aws_sdk_v_1_1
  // TODO: all AWS specific attributes should be defined in semconv package and reused cross all
  // otel packages. Related sim -
  // https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/8710
  static final AttributeKey<String> AWS_BUCKET_NAME = AttributeKey.stringKey("aws.bucket.name");
  static final AttributeKey<String> AWS_QUEUE_URL = AttributeKey.stringKey("aws.queue.url");
  static final AttributeKey<String> AWS_QUEUE_NAME = AttributeKey.stringKey("aws.queue.name");
  static final AttributeKey<String> AWS_STREAM_NAME = AttributeKey.stringKey("aws.stream.name");
  static final AttributeKey<String> AWS_TABLE_NAME = AttributeKey.stringKey("aws.table.name");
  static final AttributeKey<String> AWS_AGENT_ID = AttributeKey.stringKey("aws.bedrock.agent.id");
  static final AttributeKey<String> AWS_KNOWLEDGE_BASE_ID =
      AttributeKey.stringKey("aws.bedrock.knowledge_base.id");
  static final AttributeKey<String> AWS_DATA_SOURCE_ID =
      AttributeKey.stringKey("aws.bedrock.data_source.id");
  static final AttributeKey<String> AWS_GUARDRAIL_ID =
      AttributeKey.stringKey("aws.bedrock.guardrail.id");
  static final AttributeKey<String> AWS_GUARDRAIL_ARN =
      AttributeKey.stringKey("aws.bedrock.guardrail.arn");
}