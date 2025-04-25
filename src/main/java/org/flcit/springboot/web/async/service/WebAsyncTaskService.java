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

import java.util.concurrent.Callable;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.WebAsyncTask;

import org.flcit.commons.core.functional.runnable.RunnableException;
import org.flcit.commons.core.functional.callable.RunnableCallable;
import org.flcit.commons.core.functional.callable.RunnableExceptionCallable;
import org.flcit.springboot.web.async.configuration.WebAsyncTaskPool;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
@Service
public class WebAsyncTaskService {

    /**
     * @param timeout
     * @param executor
     * @param runnable
     * @return
     */
    public WebAsyncTask<Void> sendThrows(Long timeout, AsyncTaskExecutor executor, RunnableException runnable) {
        return send(timeout, executor, new RunnableExceptionCallable(runnable));
    }

    /**
     * @param asyncTaskPool
     * @param runnable
     * @return
     */
    public WebAsyncTask<Void> sendThrows(WebAsyncTaskPool asyncTaskPool, RunnableException runnable) {
        return send(asyncTaskPool.getTimeout(), asyncTaskPool.getExecutorName(), new RunnableExceptionCallable(runnable));
    }

    /**
     * @param timeout
     * @param executor
     * @param runnable
     * @return
     */
    public WebAsyncTask<Void> send(Long timeout, AsyncTaskExecutor executor, Runnable runnable) {
        return send(timeout, executor, new RunnableCallable(runnable));
    }

    /**
     * @param asyncTaskPool
     * @param runnable
     * @return
     */
    public WebAsyncTask<Void> send(WebAsyncTaskPool asyncTaskPool, Runnable runnable) {
        return send(asyncTaskPool.getTimeout(), asyncTaskPool.getExecutorName(), new RunnableCallable(runnable));
    }

    /**
     * @param <V>
     * @param asyncTaskPool
     * @param callable
     * @return
     */
    public <V> WebAsyncTask<V> send(WebAsyncTaskPool asyncTaskPool, Callable<V> callable) {
        return send(asyncTaskPool.getTimeout(), asyncTaskPool.getExecutorName(), callable);
    }

    /**
     * @param <V>
     * @param timeout
     * @param executorName
     * @param callable
     * @return
     */
    public <V> WebAsyncTask<V> send(Long timeout, String executorName, Callable<V> callable) {
        return new WebAsyncTask<>(timeout, executorName, callable);
    }

    /**
     * @param <V>
     * @param timeout
     * @param executor
     * @param callable
     * @return
     */
    public <V> WebAsyncTask<V> send(Long timeout, AsyncTaskExecutor executor, Callable<V> callable) {
        return new WebAsyncTask<>(getTimeout(timeout, executor), executor, callable);
    }

    private static final Long getTimeout(Long timeout, AsyncTaskExecutor executor) {
        if (timeout != null) {
            return timeout;
        }
        return executor instanceof WebAsyncTaskPool ? ((WebAsyncTaskPool) executor).getTimeout() : null;
    }

}
