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

plugins {
  java
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

base.archivesBaseName = "aws-instrumentation-awssdk-2.2"

configurations {
  /*
  We create a separate gradle configuration to grab a published Otel instrumentation agent.
  We don't need the agent during development of this extension module.
  This agent is used only during integration test.
  */
  create("otel") // Explicitly create the 'otel' configuration
}

dependencies {
  compileOnly("com.google.auto.service:auto-service:1.1.1")
  compileOnly("com.google.code.findbugs:jsr305:3.0.2")
  compileOnly("io.opentelemetry.javaagent:opentelemetry-javaagent-extension-api:1.32.1-adot2-alpha")
  compileOnly("io.opentelemetry.instrumentation:opentelemetry-instrumentation-api:1.32.1-adot2")
  compileOnly("software.amazon.awssdk:aws-json-protocol:2.2.0")
  compileOnly("software.amazon.awssdk:aws-core:2.2.0")
  compileOnly("software.amazon.awssdk:sns:2.2.0")
  compileOnly("software.amazon.awssdk:aws-json-protocol:2.2.0")
  compileOnly("org.slf4j:slf4j-api:2.0.0")
  compileOnly("org.slf4j:slf4j-simple:2.0.0")
  add("otel", "io.opentelemetry.javaagent:opentelemetry-javaagent:1.32.1-adot2")
//  compileOnly("io.opentelemetry.instrumentation:opentelemetry-aws-sdk-2.2:1.32.1-alpha")
}

tasks.register<Jar>("extendedAgent") {
  dependsOn(tasks.named("jar"))

  dependsOn(configurations.named("otel")) // Ensure the upstream agent JAR is downloaded.

  archiveFileName.set("opentelemetry-javaagent.jar") // Sets the name of the output JAR file.

  // Resolve the otel JAR file during the configuration phase
  val otelJarFile = configurations.named("otel").get().singleFile

  from(zipTree(otelJarFile)) // Unpacks the upstream OpenTelemetry agent into the new JAR.
  println("File type: ${otelJarFile::class}")

  val projectJar = tasks.named<Jar>("jar").get().archiveFile.get().asFile

  from(projectJar) {
    into("extensions") // Puts the JAR into the 'extensions' directory
  }

  doFirst {
    manifest.from(
      zipTree(otelJarFile).matching {
        include("META-INF/MANIFEST.MF")
      }.singleFile,
    )
  }
}

tasks {
  build {
    dependsOn("extendedAgent")
  }
}
