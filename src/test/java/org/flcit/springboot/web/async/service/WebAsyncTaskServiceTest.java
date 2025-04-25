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

package org.flcit.springboot.web.async.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.context.request.async.WebAsyncTask;

import org.flcit.commons.core.functional.runnable.RunnableException;
import org.flcit.springboot.commons.test.MockitoBaseTest;
import org.flcit.springboot.web.async.executor.PublicSimpleAsyncTaskExecutor;
import org.flcit.springboot.web.async.executor.PublicSimpleAsyncTaskExecutorImplements;
import org.flcit.springboot.web.async.executor.PublicThreadPoolTaskExecutor;
import org.flcit.springboot.web.async.executor.PublicThreadPoolTaskExecutorImplements;

class WebAsyncTaskServiceTest implements MockitoBaseTest {

    private static final String VALUE = "TEST";

    private static final PublicSimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new PublicSimpleAsyncTaskExecutor();
    private static final PublicSimpleAsyncTaskExecutorImplements simpleAsyncTaskExecutorImplements = new PublicSimpleAsyncTaskExecutorImplements();
    private static final PublicThreadPoolTaskExecutor threadPoolTaskExecutor = new PublicThreadPoolTaskExecutor();
    private static final PublicThreadPoolTaskExecutorImplements threadPoolTaskExecutorImplements = new PublicThreadPoolTaskExecutorImplements();

    private static WebAsyncTaskService service = new WebAsyncTaskService();

    private static BeanFactory beanFactory = mock(BeanFactory.class);

    static {
        when(beanFactory.getBean(PublicSimpleAsyncTaskExecutor.BEAN_NAME, AsyncTaskExecutor.class)).thenReturn(simpleAsyncTaskExecutor);
        when(beanFactory.getBean(simpleAsyncTaskExecutorImplements.getExecutorName(), AsyncTaskExecutor.class)).thenReturn(simpleAsyncTaskExecutorImplements);
        threadPoolTaskExecutor.initialize();
        threadPoolTaskExecutorImplements.initialize();
        when(beanFactory.getBean(PublicThreadPoolTaskExecutor.BEAN_NAME, AsyncTaskExecutor.class)).thenReturn(threadPoolTaskExecutor);
        when(beanFactory.getBean(threadPoolTaskExecutorImplements.getExecutorName(), AsyncTaskExecutor.class)).thenReturn(threadPoolTaskExecutorImplements);
    }

    @Test
    void send() throws Exception {
        simpleAsyncTaskExecutorImplements.setTimeout(5000L);
        assertAsyncTask(service.sendThrows(simpleAsyncTaskExecutorImplements, mock(RunnableException.class)), 5000L, simpleAsyncTaskExecutorImplements, null);
        assertAsyncTask(service.sendThrows(5000L, simpleAsyncTaskExecutor, mock(RunnableException.class)), 5000L, simpleAsyncTaskExecutor, null);
        assertAsyncTask(service.send(null, simpleAsyncTaskExecutor, mock(Runnable.class)), null, simpleAsyncTaskExecutor, null);
        assertAsyncTask(service.send(simpleAsyncTaskExecutorImplements, () -> VALUE), simpleAsyncTaskExecutorImplements.getTimeout(), simpleAsyncTaskExecutorImplements, VALUE);
        assertAsyncTask(service.send(simpleAsyncTaskExecutorImplements, mock(Runnable.class)), simpleAsyncTaskExecutorImplements.getTimeout(), simpleAsyncTaskExecutorImplements, null);
        
    }

    private static final <T> void assertAsyncTask(WebAsyncTask<T> asyncTask, Long timeout, AsyncTaskExecutor executor, T value) throws Exception {
        assertEquals(timeout, asyncTask.getTimeout());
        asyncTask.setBeanFactory(beanFactory);
        assertEquals(executor, asyncTask.getExecutor());
        assertEquals(value, asyncTask.getCallable().call());
    }

}
