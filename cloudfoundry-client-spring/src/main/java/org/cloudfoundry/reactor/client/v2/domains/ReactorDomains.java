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

package org.cloudfoundry.reactor.client.v2.domains;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cloudfoundry.client.v2.domains.CreateDomainRequest;
import org.cloudfoundry.client.v2.domains.CreateDomainResponse;
import org.cloudfoundry.client.v2.domains.DeleteDomainRequest;
import org.cloudfoundry.client.v2.domains.DeleteDomainResponse;
import org.cloudfoundry.client.v2.domains.Domains;
import org.cloudfoundry.client.v2.domains.GetDomainRequest;
import org.cloudfoundry.client.v2.domains.GetDomainResponse;
import org.cloudfoundry.client.v2.domains.ListDomainSpacesRequest;
import org.cloudfoundry.client.v2.domains.ListDomainSpacesResponse;
import org.cloudfoundry.client.v2.domains.ListDomainsRequest;
import org.cloudfoundry.client.v2.domains.ListDomainsResponse;
import org.cloudfoundry.reactor.client.v2.AbstractClientV2Operations;
import org.cloudfoundry.reactor.util.AuthorizationProvider;
import reactor.core.publisher.Mono;
import reactor.io.netty.http.HttpClient;

/**
 * The Reactor-based implementation of {@link Domains}
 */
public final class ReactorDomains extends AbstractClientV2Operations implements Domains {

    /**
     * Creates an instance
     *
     * @param authorizationProvider the {@link AuthorizationProvider} to use when communicating with the server
     * @param httpClient            the {@link HttpClient} to use when communicating with the server
     * @param objectMapper          the {@link ObjectMapper} to use when communicating with the server
     * @param root                  the root URI of the server.  Typically something like {@code https://uaa.run.pivotal.io}.
     */
    public ReactorDomains(AuthorizationProvider authorizationProvider, HttpClient httpClient, ObjectMapper objectMapper, Mono<String> root) {
        super(authorizationProvider, httpClient, objectMapper, root);
    }

    @Override
    public Mono<CreateDomainResponse> create(CreateDomainRequest request) {
        return post(request, CreateDomainResponse.class, builder -> builder.pathSegment("v2", "domains"));
    }

    @Override
    public Mono<DeleteDomainResponse> delete(DeleteDomainRequest request) {
        return delete(request, DeleteDomainResponse.class, builder -> builder.pathSegment("v2", "domains", request.getDomainId()));
    }

    @Override
    public Mono<GetDomainResponse> get(GetDomainRequest request) {
        return get(request, GetDomainResponse.class, builder -> builder.pathSegment("v2", "domains", request.getDomainId()));
    }

    @Override
    public Mono<ListDomainsResponse> list(ListDomainsRequest request) {
        return get(request, ListDomainsResponse.class, builder -> builder.pathSegment("v2", "domains"));
    }

    @Override
    public Mono<ListDomainSpacesResponse> listSpaces(ListDomainSpacesRequest request) {
        return get(request, ListDomainSpacesResponse.class, builder -> builder.pathSegment("v2", "domains", request.getDomainId(), "spaces"));
    }

}
