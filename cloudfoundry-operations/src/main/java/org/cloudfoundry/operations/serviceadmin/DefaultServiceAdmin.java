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

package org.cloudfoundry.operations.serviceadmin;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.servicebrokers.ListServiceBrokersRequest;
import org.cloudfoundry.client.v2.servicebrokers.ServiceBrokerEntity;
import org.cloudfoundry.client.v2.servicebrokers.ServiceBrokerResource;
import org.cloudfoundry.util.ExceptionUtils;
import org.cloudfoundry.util.PaginationUtils;
import org.cloudfoundry.util.ResourceUtils;
import org.cloudfoundry.util.ValidationUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

public final class DefaultServiceAdmin implements ServiceAdmin {

    private final CloudFoundryClient cloudFoundryClient;

    public DefaultServiceAdmin(CloudFoundryClient cloudFoundryClient) {
        this.cloudFoundryClient = cloudFoundryClient;
    }

    @Override
    public Mono<Void> deleteServiceBroker(DeleteServiceBrokerRequest request) {
        return ValidationUtils
            .validate(request)
            .then(validRequest -> getServiceBrokerId(this.cloudFoundryClient, validRequest.getName()))
            .then(serviceBrokerId -> requestDeleteServiceBroker(this.cloudFoundryClient, serviceBrokerId));
    }

    @Override
    public Flux<ServiceBroker> listServiceBrokers() {
        return requestServiceBrokers(this.cloudFoundryClient)
            .map(this::toServiceBroker);
    }

    private static Mono<ServiceBrokerResource> getServiceBroker(CloudFoundryClient cloudFoundryClient, String serviceBrokerName) {
        return requestListServiceBrokers(cloudFoundryClient, serviceBrokerName)
            .single()
            .otherwise(ExceptionUtils.replace(NoSuchElementException.class, () -> ExceptionUtils.illegalArgument("Service broker %s does not exist", serviceBrokerName)));
    }

    private static Mono<String> getServiceBrokerId(CloudFoundryClient cloudFoundryClient, String serviceBrokerName) {
        return getServiceBroker(cloudFoundryClient, serviceBrokerName)
            .map(ResourceUtils::getId);
    }

    private static Mono<Void> requestDeleteServiceBroker(CloudFoundryClient cloudFoundryClient, String serviceBrokerId) {
        return cloudFoundryClient.serviceBrokers()
            .delete(org.cloudfoundry.client.v2.servicebrokers.DeleteServiceBrokerRequest.builder()
                .serviceBrokerId(serviceBrokerId)
                .build());
    }

    private static Flux<ServiceBrokerResource> requestServiceBrokers(CloudFoundryClient cloudFoundryClient) {
        return PaginationUtils
            .requestResources(page -> cloudFoundryClient.serviceBrokers()
                .list(ListServiceBrokersRequest.builder()
                    .page(page)
                    .build()));
    }

    private static Flux<ServiceBrokerResource> requestListServiceBrokers(CloudFoundryClient cloudFoundryClient, String serviceBrokerName) {
        return PaginationUtils
            .requestResources(page -> cloudFoundryClient.serviceBrokers()
                .list(ListServiceBrokersRequest.builder()
                    .page(page)
                    .name(serviceBrokerName)
                    .build()));
    }

    private ServiceBroker toServiceBroker(ServiceBrokerResource resource) {
        ServiceBrokerEntity entity = ResourceUtils.getEntity(resource);

        return ServiceBroker.builder()
            .id(ResourceUtils.getId(resource))
            .name(entity.getName())
            .url(entity.getBrokerUrl())
            .build();
    }

}
