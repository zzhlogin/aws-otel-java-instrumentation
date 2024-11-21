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

import io.opentelemetry.api.trace.Span;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import software.amazon.awssdk.core.SdkRequest;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.utils.StringUtils;

class FieldMapper {

  private final Serializer serializer;
  private final MethodHandleFactory methodHandleFactory;

  FieldMapper() {
    serializer = new Serializer();
    methodHandleFactory = new MethodHandleFactory();
  }

  FieldMapper(Serializer serializer, MethodHandleFactory methodHandleFactory) {
    this.methodHandleFactory = methodHandleFactory;
    this.serializer = serializer;
  }

  void mapToAttributes(SdkRequest sdkRequest, ZZHAwsSdkRequest request, Span span) {
    mapToAttributes(
        field -> sdkRequest.getValueForField(field, Object.class).orElse(null),
        FieldMapping.Type.REQUEST,
        request,
        span);
  }

  void mapToAttributes(SdkResponse sdkResponse, ZZHAwsSdkRequest request, Span span) {
    mapToAttributes(
        field -> sdkResponse.getValueForField(field, Object.class).orElse(null),
        FieldMapping.Type.RESPONSE,
        request,
        span);
  }

  private void mapToAttributes(
      Function<String, Object> fieldValueProvider,
      FieldMapping.Type type,
      ZZHAwsSdkRequest request,
      Span span) {
    for (FieldMapping fieldMapping : request.fields(type)) {
      mapToAttributes(fieldValueProvider, fieldMapping, span);
    }
    for (FieldMapping fieldMapping : request.type().fields(type)) {
      mapToAttributes(fieldValueProvider, fieldMapping, span);
    }
  }

  private void mapToAttributes(
      Function<String, Object> fieldValueProvider, FieldMapping fieldMapping, Span span) {
    // traverse path
    List<String> path = fieldMapping.getFields();
    Object target = fieldValueProvider.apply(path.get(0));
    for (int i = 1; i < path.size() && target != null; i++) {
      target = next(target, path.get(i));
    }
    if (target != null) {
      String value = serializer.serialize(target);
      if (!StringUtils.isEmpty(value)) {
        span.setAttribute(fieldMapping.getAttribute(), value);
      }
    }
  }

  @Nullable
  private Object next(Object current, String fieldName) {
    try {
      return methodHandleFactory.forField(current.getClass(), fieldName).invoke(current);
    } catch (Throwable t) {
      // ignore
    }
    return null;
  }
}

//  HERE After populateRequestAttributes!!!!!!!!!!!!!:
// SdkSpan{
//  traceId=673edf6205e6a290d144e607e7d81184,
//        spanId=a930adc07653dac0,
//        parentSpanContext=ImmutableSpanContext{
//          traceId=673edf6205e6a290d144e607e7d81184,
//          spanId=0e48d172ee959008,
//          traceFlags=01,
//          traceState=ArrayBasedTraceState{entries=[]},
//          remote=false,
//          valid=true
//        },
//        name=Sns.GetTopicAttributes,
//        kind=CLIENT,
//        attributes=AttributesMap
//        {
//          data=
//          {
//            thread.id=32,
//            aws.sdk.descendant=true,
//            aws.sns.topic.arn=arn:aws:sns:us-east-1:007003802740:test_topic,
//            thread.name=http-nio-8080-exec-1,
//            aws.local.operation=GET /sns-describe-topic-v2
//          },
//          capacity=128,
//          totalAddedValues=5
//        },
//        status=ImmutableStatusData
//        {
//          statusCode=UNSET,
//          description=
//        },
//        totalRecordedEvents=0,
//        totalRecordedLinks=0,
//        startEpochNanos=1732173667015533416,
//        endEpochNanos=0
// }
//
//
// OTEL !! HERE Span span = Span.fromContext(otelContext)!!!!!!!!!!!!!:
// SdkSpan{
//  traceId=673edf6205e6a290d144e607e7d81184,
//        spanId=0e48d172ee959008,
//        parentSpanContext=ImmutableSpanContext{
//        traceId=673edf6205e6a290d144e607e7d81184,
//        spanId=a1bcd70da8da6cd1,
//        traceFlags=01,
//        traceState=ArrayBasedTraceState{entries=[]},
//        remote=false,
//        valid=true
//        },
//        name=Sns.GetTopicAttributes,
//        ind=CLIENT,
//        attributes=AttributesMap
//          {
//            data=
//            {
//              aws.agent=java-aws-sdk,
//              thread.id=32,
//              rpc.system=aws-api,
//              thread.name=http-nio-8080-exec-1,
//              aws.local.operation=GET /sns-describe-topic-v2,
//              rpc.method=GetTopicAttributes,
//              rpc.service=Sns
//            },
//            capacity=128,
//            totalAddedValues=7
//          },
//        status=ImmutableStatusData
//          {
//            statusCode=UNSET,
//          description=
//          },
//        totalRecordedEvents=0,
//        totalRecordedLinks=0, s
//        tartEpochNanos=1732173667013563416,
//        endEpochNanos=0
// }
//
//
// JAVA_TOOL_OPTIONS=" -javaagent:aws-opentelemetry-agent-1.33.0-SNAPSHOT.jar"
// OTEL_METRICS_EXPORTER=none OTEL_LOGS_EXPORT=none OTEL_AWS_APPLICATION_SIGNALS_ENABLED=true
// OTEL_AWS_APPLICATION_SIGNALS_EXPORTER_ENDPOINT=http://localhost:4316/v1/metrics
// OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
// OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=http://localhost:4316/v1/traces
// OTEL_RESOURCE_ATTRIBUTES="service.name=patch-instrumentation-service" java -jar
// springboot-0.0.1-SNAPSHOT.jar
