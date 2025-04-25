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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.flcit.commons.core.util.ThreadUtils;
import org.flcit.springboot.web.async.annotation.WebAsync;
import org.flcit.springboot.web.async.executor.PublicThreadPoolTaskExecutor;
import org.flcit.springboot.web.async.util.WebAsyncTaskUtils;

@RestController
@RequestMapping
class TestAnnotationsResource {

    static final Response RESPONSE = new Response(5, "test");

    static final String GET_POOL_NAME_RESPONSE_PATH = "/async/pool/name/response";
    static final String GET_POOL_NAME_RESPONSE_STREAM_PATH = "/async/pool/name/response/stream";

    static final String GET_POOL_EXECUTOR_RESPONSE_PATH = "/async/pool/executor/response";
    static final String GET_POOL_EXECUTOR_RESPONSE_STREAM_PATH = "/async/pool/executor/response/stream";

    static final String GET_POOL_TIMEOUT_RESPONSE_PATH = "/async/pool/timeout/response";
    static final String GET_POOL_TIMEOUT_RESPONSE_STREAM_PATH = "/async/pool/timeout/response/stream";

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping(GET_POOL_NAME_RESPONSE_PATH)
    @WebAsync(PublicThreadPoolTaskExecutor.BEAN_NAME)
    public WebAsyncTask<Response> asyncPoolNameResponse() {
        return WebAsyncTaskUtils.send(RESPONSE);
    }

    @GetMapping(GET_POOL_NAME_RESPONSE_STREAM_PATH)
    @WebAsync(PublicThreadPoolTaskExecutor.BEAN_NAME)
    public WebAsyncTask<Void> asyncPoolResponseNameStream(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), RESPONSE);
        return WebAsyncTaskUtils.send();
    }

    @GetMapping(GET_POOL_EXECUTOR_RESPONSE_PATH)
    @WebAsync(executor = PublicThreadPoolTaskExecutor.class)
    public WebAsyncTask<Response> asyncPoolExecutorResponse() {
        return WebAsyncTaskUtils.send(RESPONSE);
    }

    @GetMapping(GET_POOL_EXECUTOR_RESPONSE_STREAM_PATH)
    @WebAsync(executor = PublicThreadPoolTaskExecutor.class)
    public WebAsyncTask<Void> asyncPoolResponseExecutorStream(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), RESPONSE);
        return WebAsyncTaskUtils.send();
    }

    @GetMapping(GET_POOL_TIMEOUT_RESPONSE_PATH)
    @WebAsync(value = PublicThreadPoolTaskExecutor.BEAN_NAME, timeout = 1000)
    public WebAsyncTask<Response> asyncPoolTimeoutResponse() throws InterruptedException {
        ThreadUtils.sleep(10000);
        return WebAsyncTaskUtils.send(RESPONSE);
    }

    @GetMapping(GET_POOL_TIMEOUT_RESPONSE_STREAM_PATH)
    @WebAsync(value = PublicThreadPoolTaskExecutor.BEAN_NAME, timeout = 1000)
    public WebAsyncTask<Void> asyncPoolResponseTimeoutStream(HttpServletResponse response) throws InterruptedException, IOException {
        ThreadUtils.sleep(10000);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), RESPONSE);
        return WebAsyncTaskUtils.send();
    }

}
