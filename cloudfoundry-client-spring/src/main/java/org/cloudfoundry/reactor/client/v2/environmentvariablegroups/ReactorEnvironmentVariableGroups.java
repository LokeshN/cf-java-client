/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.reactor.client.v2.environmentvariablegroups;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cloudfoundry.client.v2.environmentvariablegroups.EnvironmentVariableGroups;
import org.cloudfoundry.client.v2.environmentvariablegroups.GetRunningEnvironmentVariablesRequest;
import org.cloudfoundry.client.v2.environmentvariablegroups.GetRunningEnvironmentVariablesResponse;
import org.cloudfoundry.client.v2.environmentvariablegroups.GetStagingEnvironmentVariablesRequest;
import org.cloudfoundry.client.v2.environmentvariablegroups.GetStagingEnvironmentVariablesResponse;
import org.cloudfoundry.client.v2.environmentvariablegroups.UpdateRunningEnvironmentVariablesRequest;
import org.cloudfoundry.client.v2.environmentvariablegroups.UpdateRunningEnvironmentVariablesResponse;
import org.cloudfoundry.client.v2.environmentvariablegroups.UpdateStagingEnvironmentVariablesRequest;
import org.cloudfoundry.client.v2.environmentvariablegroups.UpdateStagingEnvironmentVariablesResponse;
import org.cloudfoundry.reactor.client.v2.AbstractClientV2Operations;
import org.cloudfoundry.reactor.util.AuthorizationProvider;
import reactor.core.publisher.Mono;
import reactor.io.netty.http.HttpClient;

/**
 * The Reactor-based implementation of {@link EnvironmentVariableGroups}
 */
public final class ReactorEnvironmentVariableGroups extends AbstractClientV2Operations implements EnvironmentVariableGroups {

    /**
     * Creates an instance
     *
     * @param authorizationProvider the {@link AuthorizationProvider} to use when communicating with the server
     * @param httpClient            the {@link HttpClient} to use when communicating with the server
     * @param objectMapper          the {@link ObjectMapper} to use when communicating with the server
     * @param root                  the root URI of the server.  Typically something like {@code https://uaa.run.pivotal.io}.
     */
    public ReactorEnvironmentVariableGroups(AuthorizationProvider authorizationProvider, HttpClient httpClient, ObjectMapper objectMapper, Mono<String> root) {
        super(authorizationProvider, httpClient, objectMapper, root);
    }

    @Override
    public Mono<GetRunningEnvironmentVariablesResponse> getRunningEnvironmentVariables(GetRunningEnvironmentVariablesRequest request) {
        return get(request, GetRunningEnvironmentVariablesResponse.class, builder -> builder.pathSegment("v2", "config", "environment_variable_groups", "running"));
    }

    @Override
    public Mono<GetStagingEnvironmentVariablesResponse> getStagingEnvironmentVariables(GetStagingEnvironmentVariablesRequest request) {
        return get(request, GetStagingEnvironmentVariablesResponse.class, builder -> builder.pathSegment("v2", "config", "environment_variable_groups", "staging"));
    }

    @Override
    public Mono<UpdateRunningEnvironmentVariablesResponse> updateRunningEnvironmentVariables(UpdateRunningEnvironmentVariablesRequest request) {
        return put(request, UpdateRunningEnvironmentVariablesResponse.class, builder -> builder.pathSegment("v2", "config", "environment_variable_groups", "running"));
    }

    @Override
    public Mono<UpdateStagingEnvironmentVariablesResponse> updateStagingEnvironmentVariables(UpdateStagingEnvironmentVariablesRequest request) {
        return put(request, UpdateStagingEnvironmentVariablesResponse.class, builder -> builder.pathSegment("v2", "config", "environment_variable_groups", "staging"));
    }

}
