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

package org.flcit.springboot.web.async.executor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import org.flcit.springboot.web.async.configuration.WebAsyncTaskPool;

@Configuration(PublicThreadPoolTaskExecutorImplements.BEAN_NAME)
@ConfigurationProperties("web.async.public")
public class PublicThreadPoolTaskExecutorImplements extends ThreadPoolTaskExecutor implements WebAsyncTaskPool {

    private static final long serialVersionUID = 1L;
    public static final String BEAN_NAME = "PublicThreadPoolTaskExecutorImplements";
    private Long timeout;

    public PublicThreadPoolTaskExecutorImplements() {
        timeout = 60000L;
    }

    @Override
    public String getExecutorName() {
        return BEAN_NAME;
    }

    @Override
    public Long getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

}
