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

package org.flcit.springboot.web.async.util;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.web.context.request.async.WebAsyncTask;

/**
 * 
 * @since 
 * @author Florian Lestic
 */
public final class WebAsyncTaskUtils {

    private WebAsyncTaskUtils() { }

    /**
     * @param <T>
     * @param result
     * @return
     */
    public static final <T> WebAsyncTask<T> send(T result) {
        return new WebAsyncTaskResponse<>(result);
    }

    /**
     * @return
     */
    public static final WebAsyncTask<Void> send() {
        return send(null);
    }

    private static final class WebAsyncTaskResponse<T extends Object> extends WebAsyncTask<T> implements org.flcit.springboot.web.async.annotation.aspect.WebAsyncTaskResponse<T> {

        private static final Callable<?> CALLABLE = () -> { throw new IllegalStateException(); };
        private final T result;

        @SuppressWarnings("unchecked")
        public WebAsyncTaskResponse(T result) {
            super((Callable<T>) CALLABLE);
            this.result = result;
        }

        @Override
        public T getResult() {
            return result;
        }

        @Override
        public Callable<?> getCallable() {
            throw new IllegalStateException();
        }

        @Override
        public Long getTimeout() {
            throw new IllegalStateException();
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) {
            throw new IllegalStateException();
        }

        @Override
        public AsyncTaskExecutor getExecutor() {
            throw new IllegalStateException();
        }

        @Override
        public void onTimeout(Callable<T> callback) {
            throw new IllegalStateException();
        }

        @Override
        public void onError(Callable<T> callback) {
            throw new IllegalStateException();
        }

        @Override
        public void onCompletion(Runnable callback) {
            throw new IllegalStateException();
        }

    }

}
