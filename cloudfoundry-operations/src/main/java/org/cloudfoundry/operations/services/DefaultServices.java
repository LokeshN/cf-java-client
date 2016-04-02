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

package org.cloudfoundry.operations.services;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.ApplicationResource;
import org.cloudfoundry.client.v2.servicebindings.CreateServiceBindingRequest;
import org.cloudfoundry.client.v2.servicebindings.CreateServiceBindingResponse;
import org.cloudfoundry.client.v2.servicebindings.DeleteServiceBindingRequest;
import org.cloudfoundry.client.v2.servicebindings.ListServiceBindingsRequest;
import org.cloudfoundry.client.v2.servicebindings.ServiceBindingResource;
import org.cloudfoundry.client.v2.serviceinstances.ServiceInstanceResource;
import org.cloudfoundry.client.v2.spaces.ListSpaceApplicationsRequest;
import org.cloudfoundry.client.v2.spaces.ListSpaceServiceInstancesRequest;
import org.cloudfoundry.util.ExceptionUtils;
import org.cloudfoundry.util.PaginationUtils;
import org.cloudfoundry.util.ResourceUtils;
import org.cloudfoundry.util.ValidationUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.cloudfoundry.util.tuple.TupleUtils.function;

public final class DefaultServices implements Services {

    private final CloudFoundryClient cloudFoundryClient;

    private final Mono<String> spaceId;

    public DefaultServices(CloudFoundryClient cloudFoundryClient, Mono<String> spaceId) {
        this.cloudFoundryClient = cloudFoundryClient;
        this.spaceId = spaceId;
    }

    @Override
    public Mono<Void> bind(BindServiceRequest request) {
        return Mono
            .when(ValidationUtils.validate(request), this.spaceId)
            .then(function((request1, spaceId) -> Mono
                .when(
                    getSpaceServiceInstanceId(this.cloudFoundryClient, request1.getServiceName(), spaceId),
                    getApplicationId(this.cloudFoundryClient, request1.getApplicationName(), spaceId),
                    Mono.just(request1)
                )))
            .then(function((serviceInstanceId, applicationId, request1) -> requestServiceBinding(this.cloudFoundryClient, serviceInstanceId, applicationId, request1.getParameters())))
            .after();
    }

    @Override
    public Mono<Void> unbind(UnbindServiceRequest request) {
        return Mono
            .when(ValidationUtils.validate(request), this.spaceId)
            .then(function((request1, spaceId) -> Mono
                .when(
                    getSpaceServiceInstanceId(this.cloudFoundryClient, request1.getServiceName(), spaceId),
                    getApplicationId(this.cloudFoundryClient, request1.getApplicationName(), spaceId),
                    Mono.just(request1)
                )))
            .then(function((serviceInstanceId, applicationId) -> Mono
                .when(
                    Mono.just(cloudFoundryClient),
                    getServiceBindingId(this.cloudFoundryClient, serviceInstanceId, applicationId)
                )))
            .then(function((cloudFoundryClient, serviceBindingId) -> requestServiceUnbinding(cloudFoundryClient, serviceBindingId)))
            .after();
    }

    private static Mono<Void> requestServiceUnbinding(CloudFoundryClient cloudFoundryClient, String serviceBindingId) {

        return cloudFoundryClient.serviceBindings()
            .delete(DeleteServiceBindingRequest.builder()
                .serviceBindingId(serviceBindingId)
                .build());
    }

    private static Mono<String> getServiceBindingId(CloudFoundryClient cloudFoundryClient, String serviceInstanceId, String applicationId) {
        return PaginationUtils
            .requestResources(page -> cloudFoundryClient.serviceBindings()
            .list(ListServiceBindingsRequest.builder()
                .applicationId(applicationId)
                .serviceInstanceId(serviceInstanceId)
                .page(page)
                .build()))
            .single()
            .otherwise(ExceptionUtils.<ServiceBindingResource>convert("Service binding does not exist for applicationId %s and serviceInstanceId", applicationId, serviceInstanceId))
            .map(ResourceUtils::getId);
    }

    private static Mono<ApplicationResource> getApplication(CloudFoundryClient cloudFoundryClient, String applicationName, String spaceId) {
        return requestApplications(cloudFoundryClient, applicationName, spaceId)
            .single()
            .otherwise(ExceptionUtils.<ApplicationResource>convert("Application %s does not exist", applicationName));
    }

    private static Mono<String> getApplicationId(CloudFoundryClient cloudFoundryClient, String applicationName, String spaceId) {
        return getApplication(cloudFoundryClient, applicationName, spaceId)
            .map(ResourceUtils::getId);
    }

    private static Mono<ServiceInstanceResource> getSpaceServiceInstance(CloudFoundryClient cloudFoundryClient, String serviceName, String spaceId) {
        return requestSpaceServiceInstances(cloudFoundryClient, serviceName, spaceId)
            .single()
            .otherwise(ExceptionUtils.<ServiceInstanceResource>convert("Service %s does not exist", serviceName));
    }

    private static Mono<String> getSpaceServiceInstanceId(CloudFoundryClient cloudFoundryClient, String serviceName, String spaceId) {
        return getSpaceServiceInstance(cloudFoundryClient, serviceName, spaceId)
            .map(ResourceUtils::getId);
    }

    private static Flux<ApplicationResource> requestApplications(CloudFoundryClient cloudFoundryClient, String application, String spaceId) {
        return PaginationUtils
            .requestResources(page -> cloudFoundryClient.spaces()
                .listApplications(ListSpaceApplicationsRequest.builder()
                    .name(application)
                    .spaceId(spaceId)
                    .page(page)
                    .build()));
    }

    private static Mono<CreateServiceBindingResponse> requestServiceBinding(CloudFoundryClient cloudFoundryClient, String serviceInstanceId, String applicationId, Map<String, Object> parameters) {
        return cloudFoundryClient.serviceBindings()
            .create(CreateServiceBindingRequest.builder()
                .applicationId(applicationId)
                .parameters(parameters)
                .serviceInstanceId(serviceInstanceId)
                .build());
    }

    private static Flux<ServiceInstanceResource> requestSpaceServiceInstances(CloudFoundryClient cloudFoundryClient, String serviceName, String spaceId) {
        return PaginationUtils
            .requestResources(page -> cloudFoundryClient.spaces()
                .listServiceInstances(ListSpaceServiceInstancesRequest.builder()
                    .page(page)
                    .returnUserProvidedServiceInstances(true)
                    .name(serviceName)
                    .spaceId(spaceId)
                    .build()));
    }

}
