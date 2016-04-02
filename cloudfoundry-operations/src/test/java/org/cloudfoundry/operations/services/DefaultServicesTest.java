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
import org.cloudfoundry.client.v2.Resource;
import org.cloudfoundry.client.v2.applications.ApplicationResource;
import org.cloudfoundry.client.v2.servicebindings.CreateServiceBindingRequest;
import org.cloudfoundry.client.v2.servicebindings.CreateServiceBindingResponse;
import org.cloudfoundry.client.v2.servicebindings.DeleteServiceBindingRequest;
import org.cloudfoundry.client.v2.servicebindings.ListServiceBindingsRequest;
import org.cloudfoundry.client.v2.servicebindings.ListServiceBindingsResponse;
import org.cloudfoundry.client.v2.servicebindings.ServiceBindingResource;
import org.cloudfoundry.client.v2.servicebindings.ServiceBindings;
import org.cloudfoundry.client.v2.serviceinstances.ServiceInstanceResource;
import org.cloudfoundry.client.v2.spaces.ListSpaceApplicationsRequest;
import org.cloudfoundry.client.v2.spaces.ListSpaceApplicationsResponse;
import org.cloudfoundry.client.v2.spaces.ListSpaceServiceInstancesRequest;
import org.cloudfoundry.client.v2.spaces.ListSpaceServiceInstancesResponse;
import org.cloudfoundry.operations.AbstractOperationsApiTest;
import org.cloudfoundry.util.DateUtils;
import org.cloudfoundry.util.test.TestSubscriber;
import org.junit.Before;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.cloudfoundry.util.test.TestObjects.fill;
import static org.cloudfoundry.util.test.TestObjects.fillPage;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public final class DefaultServicesTest {

    private static void requestApplications(CloudFoundryClient cloudFoundryClient, String applicationName, String spaceId) {
        when(cloudFoundryClient.spaces()
            .listApplications(fillPage(ListSpaceApplicationsRequest.builder())
                .diego(null)
                .name(applicationName)
                .spaceId(spaceId)
                .build()))
            .thenReturn(Mono
                .just(fillPage(ListSpaceApplicationsResponse.builder())
                    .resource(fill(ApplicationResource.builder(), "application-")
                        .build())
                    .build()));
    }

    private static void requestApplicationsEmpty(CloudFoundryClient cloudFoundryClient, String applicationName, String spaceId) {
        when(cloudFoundryClient.spaces()
            .listApplications(fillPage(ListSpaceApplicationsRequest.builder())
                .diego(null)
                .name(applicationName)
                .spaceId(spaceId)
                .build()))
            .thenReturn(Mono
                .just(fillPage(ListSpaceApplicationsResponse.builder())
                    .build()));
    }

    private static void requestBindService(CloudFoundryClient cloudFoundryClient, String applicationId, String serviceInstanceId, Map<String, Object> parameters) {
        when(cloudFoundryClient.serviceBindings()
            .create(CreateServiceBindingRequest.builder()
                .applicationId(applicationId)
                .parameters(parameters)
                .serviceInstanceId(serviceInstanceId)
                .build()))
            .thenReturn(Mono
                .just(fill(CreateServiceBindingResponse.builder(), "service-binding-")
                    .build()));
    }

    private static void requestUnbindService(CloudFoundryClient cloudFoundryClient, String applicationId, String serviceInstanceId, String serviceBindingId) {

        ServiceBindings serviceBindings = cloudFoundryClient.serviceBindings();

        Resource.Metadata metadata = Resource.Metadata.builder().
            id(serviceBindingId)
            .build();
        //create dummy service binding resource with a default metadata
        ServiceBindingResource resource = ServiceBindingResource.builder()
            .metadata(metadata)
            .build();

        when(serviceBindings
            .list(fillPage(ListServiceBindingsRequest.builder())
                .applicationId(applicationId)
                .serviceInstanceId(serviceInstanceId)
                .page(any(Integer.class))
                .build()))
            .thenReturn(Mono
                .just(fillPage(ListServiceBindingsResponse.builder().resource(resource))
                    .build()));;

        when(serviceBindings
            .delete(DeleteServiceBindingRequest.builder()
            .serviceBindingId(serviceBindingId)
            .build()))
            .thenReturn(Mono.<Void>empty());

    }

    private static void requestSpaceServiceInstances(CloudFoundryClient cloudFoundryClient, String serviceName, String spaceId) {
        when(cloudFoundryClient.spaces()
            .listServiceInstances(fillPage(ListSpaceServiceInstancesRequest.builder())
                .spaceId(spaceId)
                .name(serviceName)
                .build()))
            .thenReturn(Mono
                .just(fillPage(ListSpaceServiceInstancesResponse.builder())
                    .resource(fill(ServiceInstanceResource.builder(), "service-instance-")
                        .build())
                    .build()));
    }

    private static void requestSpaceServiceInstancesEmpty(CloudFoundryClient cloudFoundryClient, String serviceName, String spaceId) {
        when(cloudFoundryClient.spaces()
            .listServiceInstances(fillPage(ListSpaceServiceInstancesRequest.builder())
                .spaceId(spaceId)
                .name(serviceName)
                .build()))
            .thenReturn(Mono
                .just(fillPage(ListSpaceServiceInstancesResponse.builder())
                    .build()));
    }

    public static final class BindService extends AbstractOperationsApiTest<Void> {

        private final DefaultServices services = new DefaultServices(this.cloudFoundryClient, Mono.just(TEST_SPACE_ID));

        @Before
        public void setUp() throws Exception {
            requestApplications(this.cloudFoundryClient, "test-application-name", TEST_SPACE_ID);
            requestSpaceServiceInstances(this.cloudFoundryClient, "test-service-name", TEST_SPACE_ID);
            requestBindService(this.cloudFoundryClient, "test-application-id", "test-service-instance-id", Collections.<String, Object>singletonMap("test-parameter-key", "test-parameter-value"));
        }

        @Override
        protected void assertions(TestSubscriber<Void> testSubscriber) throws Exception {
            // Expects onComplete() with no onNext()
        }

        @Override
        protected Mono<Void> invoke() {
            return this.services
                .bind(BindServiceRequest.builder()
                    .applicationName("test-application-name")
                    .serviceName("test-service-name")
                    .parameter("test-parameter-key", "test-parameter-value")
                    .build());
        }

    }

    public static final class BindServiceNoApplication extends AbstractOperationsApiTest<Void> {

        private final DefaultServices services = new DefaultServices(this.cloudFoundryClient, Mono.just(TEST_SPACE_ID));

        @Before
        public void setUp() throws Exception {
            requestApplicationsEmpty(this.cloudFoundryClient, "test-application-name", TEST_SPACE_ID);
            requestSpaceServiceInstances(this.cloudFoundryClient, "test-service-name", TEST_SPACE_ID);
        }

        @Override
        protected void assertions(TestSubscriber<Void> testSubscriber) throws Exception {
            testSubscriber
                .assertError(IllegalArgumentException.class, "Application test-application-name does not exist");
        }

        @Override
        protected Mono<Void> invoke() {
            return this.services
                .bind(BindServiceRequest.builder()
                    .applicationName("test-application-name")
                    .serviceName("test-service-name")
                    .parameter("test-parameter-key", "test-parameter-value")
                    .build());
        }

    }

    public static final class BindServiceNoService extends AbstractOperationsApiTest<Void> {

        private final DefaultServices services = new DefaultServices(this.cloudFoundryClient, Mono.just(TEST_SPACE_ID));

        @Before
        public void setUp() throws Exception {
            requestApplications(this.cloudFoundryClient, "test-application-name", TEST_SPACE_ID);
            requestSpaceServiceInstancesEmpty(this.cloudFoundryClient, "test-service-name", TEST_SPACE_ID);
        }

        @Override
        protected void assertions(TestSubscriber<Void> testSubscriber) throws Exception {
            testSubscriber
                .assertError(IllegalArgumentException.class, "Service test-service-name does not exist");
        }

        @Override
        protected Mono<Void> invoke() {
            return this.services
                .bind(BindServiceRequest.builder()
                    .applicationName("test-application-name")
                    .serviceName("test-service-name")
                    .build());
        }

    }

    public static final class UnbindService extends AbstractOperationsApiTest<Void> {

        private final DefaultServices services = new DefaultServices(this.cloudFoundryClient, Mono.just(TEST_SPACE_ID));

        @Before
        public void setUp() throws Exception {
            final String appName = "test-application-name";
            final String serviceName = "test-service-name";
            final String serviceBindingId = "test-service-binding-id";
            requestApplications(this.cloudFoundryClient, appName, TEST_SPACE_ID);
            requestSpaceServiceInstances(this.cloudFoundryClient, serviceName, TEST_SPACE_ID);
            requestUnbindService(this.cloudFoundryClient, appName, serviceName, serviceBindingId);
        }

        @Override
        protected void assertions(TestSubscriber<Void> testSubscriber) throws Exception {
            // Expects onComplete() with no onNext()
        }

        @Override
        protected Mono<Void> invoke() {
            return this.services
                .unbind(UnbindServiceRequest.builder()
                    .applicationName("test-application-name")
                    .serviceName("test-service-name")
                    .build());
        }

    }

    public static final class UnbindServiceNoApplication extends AbstractOperationsApiTest<Void> {

        private final DefaultServices services = new DefaultServices(this.cloudFoundryClient, Mono.just(TEST_SPACE_ID));

        @Before
        public void setUp() throws Exception {
            requestApplicationsEmpty(this.cloudFoundryClient, "test-application-name", TEST_SPACE_ID);
            requestSpaceServiceInstances(this.cloudFoundryClient, "test-service-name", TEST_SPACE_ID);
        }

        @Override
        protected void assertions(TestSubscriber<Void> testSubscriber) throws Exception {
            testSubscriber
                .assertError(IllegalArgumentException.class, "Application test-application-name does not exist");
        }

        @Override
        protected Mono<Void> invoke() {
            return this.services
                .unbind(UnbindServiceRequest.builder()
                    .applicationName("test-application-name")
                    .serviceName("test-service-name")
                    .build());
        }

    }

    public static final class UnbindServiceNoService extends AbstractOperationsApiTest<Void> {

        private final DefaultServices services = new DefaultServices(this.cloudFoundryClient, Mono.just(TEST_SPACE_ID));

        @Before
        public void setUp() throws Exception {
            requestApplications(this.cloudFoundryClient, "test-application-name", TEST_SPACE_ID);
            requestSpaceServiceInstancesEmpty(this.cloudFoundryClient, "test-service-name", TEST_SPACE_ID);
        }

        @Override
        protected void assertions(TestSubscriber<Void> testSubscriber) throws Exception {
            testSubscriber
                .assertError(IllegalArgumentException.class, "Service test-service-name does not exist");
        }

        @Override
        protected Mono<Void> invoke() {
            return this.services
                .unbind(UnbindServiceRequest.builder()
                    .applicationName("test-application-name")
                    .serviceName("test-service-name")
                    .build());
        }

    }


}
