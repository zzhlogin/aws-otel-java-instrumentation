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

import static software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsExperimentalAttributes.GEN_AI_SYSTEM;
import static software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsSdkRequestType.BEDROCKRUNTIME;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import java.time.Instant;
import java.util.logging.Logger;
import software.amazon.awssdk.auth.signer.AwsSignerExecutionAttribute;
import software.amazon.awssdk.core.SdkRequest;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttribute;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.http.SdkHttpResponse;

/**
 * A {@link ExecutionInterceptor} for use as an SPI by the AWS SDK to automatically trace all
 * requests.
 */
public class TracingExecutionInterceptor implements ExecutionInterceptor {

  //  private final ExecutionInterceptor delegate =
  //      AwsSdkSingletons.telemetry().newExecutionInterceptor();
  private static final String GEN_AI_SYSTEM_BEDROCK = "aws_bedrock";
  private static final ExecutionAttribute<io.opentelemetry.context.Context> CONTEXT_ATTRIBUTE =
      new ExecutionAttribute<>(TracingExecutionInterceptor.class.getName() + ".Context");
  private static final ExecutionAttribute<RequestSpanFinisher> REQUEST_FINISHER_ATTRIBUTE =
      new ExecutionAttribute<>(TracingExecutionInterceptor.class.getName() + ".RequestFinisher");
  private static final ExecutionAttribute<ZZHAwsSdkRequest> AWS_SDK_REQUEST_ATTRIBUTE =
      new ExecutionAttribute<>(TracingExecutionInterceptor.class.getName() + ".ZZHAwsSdkRequest");
  private final AwsAdotInstrumenterFactory instrumenterFactory =
      new AwsAdotInstrumenterFactory(GlobalOpenTelemetry.get());

  private final FieldMapper fieldMapper = new FieldMapper();
  private static final Logger logger =
      Logger.getLogger(TracingExecutionInterceptor.class.getName());

  @Override // !!!!!!!!Important
  public SdkRequest modifyRequest(
      Context.ModifyRequest context, ExecutionAttributes executionAttributes) {
    System.out.println("HERE TracingExecutionInterceptor!!!!!!!!!!!!!: ");
    io.opentelemetry.context.Context parentOtelContext = io.opentelemetry.context.Context.current();
    SdkRequest request = context.request();

    // Ignore presign request. These requests don't run all interceptor methods and the span
    // here would never be ended and scope closed.
    if (executionAttributes.getAttribute(AwsSignerExecutionAttribute.PRESIGNER_EXPIRATION)
        != null) {
      return request;
    }

    Instrumenter<ExecutionAttributes, Response> instrumenter =
        instrumenterFactory.requestInstrumenter();

    if (!instrumenter.shouldStart(parentOtelContext, executionAttributes)) {
      // NB: We also skip injection in case we don't start.
      return request;
    }

    Instant requestStart = Instant.now();
    io.opentelemetry.context.Context otelContext =
        instrumenter.start(parentOtelContext, executionAttributes);
    RequestSpanFinisher requestFinisher = instrumenter::end;

    Span span = Span.fromContext(otelContext);

    try {
      ZZHAwsSdkRequest awsSdkRequest = ZZHAwsSdkRequest.ofSdkRequest(context.request());
      if (awsSdkRequest != null) {
        populateRequestAttributes(span, awsSdkRequest, context.request(), executionAttributes);
      }
    } catch (Throwable throwable) {
      requestFinisher.finish(otelContext, executionAttributes, null, throwable);
      clearAttributes(executionAttributes);
      throw throwable;
    }
    return request;
  }

  @Override // !!!!!!!!Important
  public void afterExecution(
      Context.AfterExecution context, ExecutionAttributes executionAttributes) {

    io.opentelemetry.context.Context otelContext = getContext(executionAttributes);
    if (otelContext != null) {
      Span span = Span.fromContext(otelContext);
      onSdkResponse(span, context.response(), executionAttributes);

      SdkHttpResponse httpResponse = context.httpResponse();

      RequestSpanFinisher finisher = executionAttributes.getAttribute(REQUEST_FINISHER_ATTRIBUTE);
      finisher.finish(
          otelContext, executionAttributes, new Response(httpResponse, context.response()), null);
    }
    clearAttributes(executionAttributes);
  }

  private void populateRequestAttributes(
      Span span,
      ZZHAwsSdkRequest awsSdkRequest,
      SdkRequest sdkRequest,
      ExecutionAttributes attributes) {
    System.out.println("HERE before populateRequestAttributes!!!!!!!!!!!!!: ");
    System.out.println(span);

    fieldMapper.mapToAttributes(sdkRequest, awsSdkRequest, span);

    if (awsSdkRequest.type() == BEDROCKRUNTIME) {
      span.setAttribute(GEN_AI_SYSTEM, GEN_AI_SYSTEM_BEDROCK);
    }
    System.out.println("HERE After populateRequestAttributes!!!!!!!!!!!!!: ");
    System.out.println(span);
  }

  private void onSdkResponse(
      Span span, SdkResponse response, ExecutionAttributes executionAttributes) {
    ZZHAwsSdkRequest sdkRequest = executionAttributes.getAttribute(AWS_SDK_REQUEST_ATTRIBUTE);
    if (sdkRequest != null) {
      fieldMapper.mapToAttributes(response, sdkRequest, span);
    }
  }

  private static void clearAttributes(ExecutionAttributes executionAttributes) {
    executionAttributes.putAttribute(AWS_SDK_REQUEST_ATTRIBUTE, null);
  }

  static io.opentelemetry.context.Context getContext(ExecutionAttributes attributes) {
    return attributes.getAttribute(CONTEXT_ATTRIBUTE);
  }

  private interface RequestSpanFinisher {
    void finish(
        io.opentelemetry.context.Context otelContext,
        ExecutionAttributes executionAttributes,
        Response response,
        Throwable exception);
  }

  //  @Override
  //  public SdkHttpResponse modifyHttpResponse(
  //          Context.ModifyHttpResponse context, ExecutionAttributes executionAttributes) {
  //    return delegate.modifyHttpResponse(context, executionAttributes);
  //  }
  //  @Override
  //  public SdkResponse modifyResponse(
  //          Context.ModifyResponse context, ExecutionAttributes executionAttributes) {
  //    return delegate.modifyResponse(context, executionAttributes);
  //  }
  //  @Override
  //  public void onExecutionFailure(
  //          Context.FailedExecution context, ExecutionAttributes executionAttributes) {
  //    delegate.onExecutionFailure(context, executionAttributes);
  //  }
  //
  //  @Override
  //  public void beforeTransmission(
  //          Context.BeforeTransmission context, ExecutionAttributes executionAttributes) {
  //    delegate.beforeTransmission(context, executionAttributes);
  //  }
  //
  //  @Override
  //  public Optional<InputStream> modifyHttpResponseContent(
  //          Context.ModifyHttpResponse context, ExecutionAttributes executionAttributes) {
  //    return delegate.modifyHttpResponseContent(context, executionAttributes);
  //  }
  //
  //  @Override
  //  public SdkHttpRequest modifyHttpRequest(
  //          Context.ModifyHttpRequest context, ExecutionAttributes executionAttributes) {
  //    return delegate.modifyHttpRequest(context, executionAttributes);
  //  }
  //  @Override
  //  public void beforeExecution(
  //      Context.BeforeExecution context, ExecutionAttributes executionAttributes) {
  //    delegate.beforeExecution(context, executionAttributes);
  //  }
  //
  //  @Override
  //  public void beforeMarshalling(
  //      Context.BeforeMarshalling context, ExecutionAttributes executionAttributes) {
  //    delegate.beforeMarshalling(context, executionAttributes);
  //  }
  //
  //  @Override
  //  public void afterMarshalling(
  //      Context.AfterMarshalling context, ExecutionAttributes executionAttributes) {
  //    delegate.afterMarshalling(context, executionAttributes);
  //  }
  //
  //  @Override
  //  public Optional<RequestBody> modifyHttpContent(
  //      Context.ModifyHttpRequest context, ExecutionAttributes executionAttributes) {
  //    return delegate.modifyHttpContent(context, executionAttributes);
  //  }
  //
  //  @Override
  //  public Optional<AsyncRequestBody> modifyAsyncHttpContent(
  //      Context.ModifyHttpRequest context, ExecutionAttributes executionAttributes) {
  //    return delegate.modifyAsyncHttpContent(context, executionAttributes);
  //  }
  //
  //  @Override
  //  public void afterTransmission(
  //      Context.AfterTransmission context, ExecutionAttributes executionAttributes) {
  //    delegate.afterTransmission(context, executionAttributes);
  //  }
  //
  //
  //  @Override
  //  public Optional<Publisher<ByteBuffer>> modifyAsyncHttpResponseContent(
  //      Context.ModifyHttpResponse context, ExecutionAttributes executionAttributes) {
  //    return delegate.modifyAsyncHttpResponseContent(context, executionAttributes);
  //  }
  //
  //
  //  @Override
  //  public void beforeUnmarshalling(
  //      Context.BeforeUnmarshalling context, ExecutionAttributes executionAttributes) {
  //    delegate.beforeUnmarshalling(context, executionAttributes);
  //  }
  //
  //  @Override
  //  public void afterUnmarshalling(
  //      Context.AfterUnmarshalling context, ExecutionAttributes executionAttributes) {
  //    delegate.afterUnmarshalling(context, executionAttributes);
  //  }
  //
  //

  //
  //  @Override
  //  public Throwable modifyException(
  //      Context.FailedExecution context, ExecutionAttributes executionAttributes) {
  //    return delegate.modifyException(context, executionAttributes);
  //  }
}
