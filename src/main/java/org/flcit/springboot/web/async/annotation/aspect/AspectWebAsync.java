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

package org.flcit.springboot.web.async.annotation.aspect;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.flcit.commons.core.functional.callable.CallableThrowable;
import org.flcit.commons.core.util.ObjectUtils;
import org.flcit.commons.core.util.StringUtils;
import org.flcit.springboot.commons.core.util.BeanUtils;
import org.flcit.springboot.web.async.annotation.WebAsync;
import org.flcit.springboot.web.async.configuration.WebAsyncTaskPool;
import org.flcit.springboot.web.async.service.WebAsyncTaskService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.async.WebAsyncTask;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
@Aspect
public class AspectWebAsync implements BeanFactoryAware {

    private final Map<Method, AsyncTaskExecutor> executors = new ConcurrentHashMap<>(16);

    @Nullable
    private BeanFactory beanFactory;

    private final WebAsyncTaskService webAsyncTaskService;

    /**
     * @param webAsyncTaskService
     */
    public AspectWebAsync(WebAsyncTaskService webAsyncTaskService) {
        this.webAsyncTaskService = webAsyncTaskService;
    }

    /**
     * @param joinPoint
     * @param webAsync
     * @return
     * @throws Throwable
     */
    @Around("@annotation(webAsync)")
    public WebAsyncTask<Object> activate(final ProceedingJoinPoint joinPoint, final WebAsync webAsync) throws Throwable {
        final AsyncTaskExecutor executor = getAsyncTaskExecutor(((MethodSignature) joinPoint.getSignature()).getMethod(), webAsync);
        return webAsyncTaskService.send(getTimeout(executor, webAsync), executor, new CallableThrowable<>(() -> getResponse(joinPoint.proceed())));
    }

    private static Long getTimeout(final AsyncTaskExecutor executor, final WebAsync webAsync) {
        if (webAsync.timeout() == -1) {
            return null;
        } else if (webAsync.timeout() > 0) {
            return webAsync.timeout();
        } else if (executor instanceof WebAsyncTaskPool) {
            return ((WebAsyncTaskPool) executor).getTimeout();
        }
        return null;
    }

    private AsyncTaskExecutor getAsyncTaskExecutor(final Method method, final WebAsync webAsync) {
        return this.executors.computeIfAbsent(method, m -> {
             AsyncTaskExecutor executor = BeanUtils.getOptionalByNameOrClass(
                     this.beanFactory,
                     StringUtils.nullIfEmpty(webAsync.value()),
                     AsyncTaskExecutor.class,
                     ObjectUtils.nullIfEquals(webAsync.executor(), AsyncTaskExecutor.class));
             if (executor == null) {
                 executor = new SimpleAsyncTaskExecutor();
             }
             if (!(executor instanceof AsyncListenableTaskExecutor)) {
                 executor = new TaskExecutorAdapter(executor);
             }
             return executor;
        });
    }

    private static Object getResponse(Object result) {
        return ((WebAsyncTaskResponse<?>) result).getResult();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

}
