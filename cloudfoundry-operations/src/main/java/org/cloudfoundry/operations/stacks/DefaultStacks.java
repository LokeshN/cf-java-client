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

package org.cloudfoundry.operations.stacks;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.stacks.ListStacksRequest;
import org.cloudfoundry.client.v2.stacks.StackResource;
import org.cloudfoundry.util.ExceptionUtils;
import org.cloudfoundry.util.PaginationUtils;
import org.cloudfoundry.util.ResourceUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

public final class DefaultStacks implements Stacks {

    private final CloudFoundryClient cloudFoundryClient;

    public DefaultStacks(CloudFoundryClient cloudFoundryClient) {
        this.cloudFoundryClient = cloudFoundryClient;
    }

    @Override
    public Mono<Stack> get(GetStackRequest request) {
        return getStack(this.cloudFoundryClient, request.getName())
            .map(this::toStack);
    }

    @Override
    public Flux<Stack> list() {
        return requestStacks(this.cloudFoundryClient)
            .map(this::toStack);
    }

    private static Mono<StackResource> getStack(CloudFoundryClient cloudFoundryClient, String stack) {
        return requestStack(cloudFoundryClient, stack)
            .single()
            .otherwise(NoSuchElementException.class, t -> ExceptionUtils.illegalArgument("Stack %s does not exist", stack));
    }

    private static Flux<StackResource> requestStack(CloudFoundryClient cloudFoundryClient, String stack) {
        return PaginationUtils
            .requestResources(page -> cloudFoundryClient.stacks().list(
                ListStacksRequest.builder()
                    .name(stack)
                    .page(page)
                    .build()));
    }

    private static Flux<StackResource> requestStacks(CloudFoundryClient cloudFoundryClient) {
        return PaginationUtils
            .requestResources(page -> cloudFoundryClient.stacks().list(
                ListStacksRequest.builder()
                    .page(page)
                    .build()));
    }

    private Stack toStack(StackResource stackResource) {
        return Stack.builder()
            .description(ResourceUtils.getEntity(stackResource).getDescription())
            .id(ResourceUtils.getId(stackResource))
            .name(ResourceUtils.getEntity(stackResource).getName())
            .build();
    }

}
