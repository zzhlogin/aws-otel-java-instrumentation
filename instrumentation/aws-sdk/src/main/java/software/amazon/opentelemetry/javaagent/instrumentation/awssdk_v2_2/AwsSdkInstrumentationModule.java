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

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.bytebuddy.matcher.ElementMatchers.named;

import com.google.auto.service.AutoService;
import io.opentelemetry.javaagent.extension.instrumentation.HelperResourceBuilder;
import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;
import java.util.List;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

@AutoService(InstrumentationModule.class)
public class AwsSdkInstrumentationModule extends InstrumentationModule {
  public AwsSdkInstrumentationModule() {
    super("aws-sdk", "aws-sdk-2.2", "aws-sdk-2.2-core");
    System.out.println("HERE AwsSdkInstrumentationModule!!!!!!!!!!!!!: ");
  }

  @Override // Need
  public int order() {
    return 1;
  }

  @Override
  public boolean isIndyModule() {
    return false;
  }

  @Override // Need
  public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
    System.out.println("HERE classLoaderMatcher!!!!!!!!!!!!!: ");
    System.out.println("java.class.path!!!!!!!!!!!!!: ");
    System.out.println(System.getProperty("java.class.path"));
    // We don't actually transform it but want to make sure we only apply the instrumentation when
    // our key dependency is present.
    return hasClassesNamed("software.amazon.awssdk.core.interceptor.ExecutionInterceptor");
  }

  @Override
  public void registerHelperResources(HelperResourceBuilder helperResourceBuilder) {
    System.out.println("HERE registerHelperResources!!!!!!!!!!!!!: ");
    helperResourceBuilder.register("software/amazon/awssdk/global/handlers/execution.interceptors");
  }

  @Override
  public List<String> getAdditionalHelperClassNames() {
    return asList(
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.TracingExecutionInterceptor",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.TracingExecutionInterceptor$RequestSpanFinisher",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.ZZHAwsSdkRequest",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsAdotInstrumenterFactory",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.FieldMapper",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.FieldMapping",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.FieldMapping$Type",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsExperimentalAttributes",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsSdkExperimentalAttributesExtractor",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.MethodHandleFactory",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.MethodHandleFactory$1",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.Serializer",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AWSExperimentalAttributes",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AWSJsonProtocolFactoryAccess",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.AwsSdkRequestType",
        "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.Response");
  }

  //  @Override
  //  public void injectClasses(ClassInjector injector) {
  //    injector
  //        .proxyBuilder(
  //
  // "software.amazon.opentelemetry.javaagent.instrumentation.awssdk_v2_2.TracingExecutionInterceptor")
  //        .inject(InjectionMode.CLASS_ONLY);
  //  }

  @Override // Need
  public List<TypeInstrumentation> typeInstrumentations() {
    System.out.println("HERE typeInstrumentations!!!!!!!!!!!!!: ");
    return singletonList(new ResourceInjectingTypeInstrumentation());
  }

  // A type instrumentation is needed to trigger resource injection. // Need
  public class ResourceInjectingTypeInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
      System.out.println("HERE typeMatcher!!!!!!!!!!!!!: ");
      // This is essentially the entry point of the AWS SDK, all clients implement it. We can ensure
      // our interceptor service definition is injected as early as possible if we typematch against
      // it.
      return named("software.amazon.awssdk.core.SdkClient");
    }

    @Override
    public void transform(TypeTransformer transformer) {
      System.out.println("HERE transform!!!!!!!!!!!!!: ");
      doTransform(transformer);
    }
  }

  void doTransform(TypeTransformer transformer) {
    // Nothing to transform, this type instrumentation is only used for injecting resources.
  }
}
