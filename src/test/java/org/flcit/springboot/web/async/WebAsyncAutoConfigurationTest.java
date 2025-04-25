/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.flcit.springboot.web.async;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import org.flcit.springboot.commons.test.util.MvcUtils;
import org.flcit.springboot.web.async.annotation.aspect.AspectWebAsync;
import org.flcit.springboot.web.async.executor.PublicThreadPoolTaskExecutor;
import org.flcit.springboot.web.async.service.WebAsyncTaskService;

class WebAsyncAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    WebMvcAutoConfiguration.class,
                    AopAutoConfiguration.class,
                    WebAsyncAutoConfiguration.class)
             );

    @Test
    void beans() {
        this.contextRunner
        .run(context -> {
            assertThat(context).doesNotHaveBean(AspectWebAsync.class);
            assertThat(context).hasSingleBean(WebAsyncTaskService.class);
        });
        this.contextRunner
        .withPropertyValues("async.web.annotation=true")
        .run(context -> {
            assertThat(context).hasSingleBean(AspectWebAsync.class);
            assertThat(context).hasSingleBean(WebAsyncTaskService.class);
        });
    }

    @Test
    void annotation() {
        this.contextRunner
        .withPropertyValues("async.web.annotation=true")
        .withUserConfiguration(PublicThreadPoolTaskExecutor.class, TestAnnotationsResource.class)
        .run(context -> {
            MvcUtils.assertGetJsonAsyncResponses(context, TestAnnotationsResource.RESPONSE,
                    new String[] {
                            TestAnnotationsResource.GET_POOL_NAME_RESPONSE_PATH,
                            TestAnnotationsResource.GET_POOL_NAME_RESPONSE_STREAM_PATH,
                            TestAnnotationsResource.GET_POOL_EXECUTOR_RESPONSE_PATH,
                            TestAnnotationsResource.GET_POOL_EXECUTOR_RESPONSE_STREAM_PATH
                    }
            );
            MvcUtils.assertGetAsyncResponsesTimeout(context,
                    new String[] {
                            TestAnnotationsResource.GET_POOL_TIMEOUT_RESPONSE_PATH,
                            TestAnnotationsResource.GET_POOL_TIMEOUT_RESPONSE_STREAM_PATH
                    }
            );
        });
    }

}
