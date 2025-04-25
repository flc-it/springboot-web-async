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

package org.flcit.springboot.web.async.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.flcit.commons.core.functional.callable.CallableThrowable;
import org.flcit.springboot.commons.test.MockitoBaseTest;
import org.flcit.springboot.web.async.annotation.aspect.AspectWebAsync;
import org.flcit.springboot.web.async.executor.BasicAsyncTaskExecutor;
import org.flcit.springboot.web.async.executor.PublicSimpleAsyncTaskExecutorImplements;
import org.flcit.springboot.web.async.service.WebAsyncTaskService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.context.request.async.WebAsyncTask;

class AspectWebAsyncTest implements MockitoBaseTest {

    private static final Method METHOD;

    @Mock
    private ProceedingJoinPoint proceedingJoinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private WebAsyncTaskService webAsyncTaskService;

    @Mock
    private BeanFactory beanFactory;

    static {
        try {
            METHOD = TestClass.class.getDeclaredMethod("getObject");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final WebAsync WEB_ASYNC_TIMEOUT = new WebAsync() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return WebAsync.class;
        }
        @Override
        public String value() {
            return "test";
        }
        @Override
        public long timeout() {
            return -1;
        }
        @Override
        public Class<? extends AsyncTaskExecutor> executor() {
            return AsyncTaskExecutor.class;
        }
    };

    private static final WebAsync WEB_ASYNC_EXECUTOR = new WebAsync() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return WebAsync.class;
        }
        @Override
        public String value() {
            return null;
        }
        @Override
        public long timeout() {
            return 0;
        }
        @Override
        public Class<? extends AsyncTaskExecutor> executor() {
            return PublicSimpleAsyncTaskExecutorImplements.class;
        }
    };

    private static final WebAsync WEB_ASYNC_NO_EXECUTOR = new WebAsync() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return WebAsync.class;
        }
        @Override
        public String value() {
            return null;
        }
        @Override
        public long timeout() {
            return -1;
        }
        @Override
        public Class<? extends AsyncTaskExecutor> executor() {
            return AsyncTaskExecutor.class;
        }
    };

    @SuppressWarnings("unchecked")
    @Test
    void activateTest() throws Throwable {
        when(methodSignature.getMethod()).thenReturn(METHOD);
        when(proceedingJoinPoint.getSignature()).thenReturn(methodSignature);
        when(beanFactory.getBean(anyString(), eq(AsyncTaskExecutor.class))).thenReturn(null);
        when(webAsyncTaskService.send(isNull(), any(AsyncTaskExecutor.class), any(Callable.class))).thenCallRealMethod();
        WebAsyncTask<Object> webAsyncTask = getAspectWebAsync().activate(proceedingJoinPoint, WEB_ASYNC_TIMEOUT);
        assertInstanceOf(SimpleAsyncTaskExecutor.class, webAsyncTask.getExecutor());
        assertNull(webAsyncTask.getTimeout());
        assertInstanceOf(CallableThrowable.class, webAsyncTask.getCallable());

        final PublicSimpleAsyncTaskExecutorImplements executor = new PublicSimpleAsyncTaskExecutorImplements();
        executor.setTimeout(60000L);
        when(beanFactory.getBean(PublicSimpleAsyncTaskExecutorImplements.class)).thenReturn(executor);
        when(webAsyncTaskService.send(anyLong(), any(AsyncTaskExecutor.class), any(Callable.class))).thenCallRealMethod();
        final AspectWebAsync aspectWebAsync = getAspectWebAsync();
        webAsyncTask = aspectWebAsync.activate(proceedingJoinPoint, WEB_ASYNC_EXECUTOR);
        assertEquals(60000L, webAsyncTask.getTimeout());
        assertInstanceOf(PublicSimpleAsyncTaskExecutorImplements.class, webAsyncTask.getExecutor());

        webAsyncTask = aspectWebAsync.activate(proceedingJoinPoint, WEB_ASYNC_TIMEOUT);
        assertEquals(executor, webAsyncTask.getExecutor());

        webAsyncTask = getAspectWebAsync().activate(proceedingJoinPoint, WEB_ASYNC_NO_EXECUTOR);
        assertInstanceOf(SimpleAsyncTaskExecutor.class, webAsyncTask.getExecutor());
        when(beanFactory.getBean(anyString(), eq(AsyncTaskExecutor.class))).thenReturn(new BasicAsyncTaskExecutor());
        webAsyncTask = getAspectWebAsync().activate(proceedingJoinPoint, WEB_ASYNC_TIMEOUT);
        assertInstanceOf(AsyncListenableTaskExecutor.class, webAsyncTask.getExecutor());
    }

    private AspectWebAsync getAspectWebAsync() {
        final AspectWebAsync aspectWebAsync = new AspectWebAsync(webAsyncTaskService);
        aspectWebAsync.setBeanFactory(beanFactory);
        return aspectWebAsync;
    }

    static class TestClass {
        Object getObject() {
            return new Object();
        }
    }

}
