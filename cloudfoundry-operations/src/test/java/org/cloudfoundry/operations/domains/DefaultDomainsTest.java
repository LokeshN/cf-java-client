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

package org.cloudfoundry.operations.domains;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.domains.DomainEntity;
import org.cloudfoundry.client.v2.domains.DomainResource;
import org.cloudfoundry.client.v2.domains.ListDomainsRequest;
import org.cloudfoundry.client.v2.domains.ListDomainsResponse;
import org.cloudfoundry.client.v2.organizations.ListOrganizationsRequest;
import org.cloudfoundry.client.v2.organizations.ListOrganizationsResponse;
import org.cloudfoundry.client.v2.organizations.OrganizationResource;
import org.cloudfoundry.client.v2.privatedomains.CreatePrivateDomainRequest;
import org.cloudfoundry.client.v2.privatedomains.CreatePrivateDomainResponse;
import org.cloudfoundry.client.v2.shareddomains.CreateSharedDomainResponse;
import org.cloudfoundry.operations.AbstractOperationsApiTest;
import org.cloudfoundry.util.RequestValidationException;
import org.cloudfoundry.util.test.TestSubscriber;
import org.junit.Before;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import static org.cloudfoundry.util.test.TestObjects.fill;
import static org.cloudfoundry.util.test.TestObjects.fillPage;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public final class DefaultDomainsTest {

    private static void requestCreateDomain(CloudFoundryClient cloudFoundryClient, String domain, String organizationId) {
        when(cloudFoundryClient.privateDomains()
            .create(CreatePrivateDomainRequest.builder()
                .name(domain)
                .owningOrganizationId(organizationId)
                .build()))
            .thenReturn(Mono
                .just(fill(CreatePrivateDomainResponse.builder(), "private-domain-")
                    .build()));
    }

    private static void requestCreateSharedDomain(CloudFoundryClient cloudFoundryClient, String domain) {
        when(cloudFoundryClient.sharedDomains()
            .create(org.cloudfoundry.client.v2.shareddomains.CreateSharedDomainRequest.builder()
                .name(domain)
                .build()))
            .thenReturn(Mono
                .just(fill(CreateSharedDomainResponse.builder(), "shared-domain-")
                    .build()));
    }

    private static void requestOrganizations(CloudFoundryClient cloudFoundryClient, String organization) {
        when(cloudFoundryClient.organizations()
            .list(fillPage(ListOrganizationsRequest.builder())
                .name(organization)
                .build()))
            .thenReturn(Mono
                .just(fillPage(ListOrganizationsResponse.builder())
                    .resource(fill(OrganizationResource.builder(), "organization-")
                        .build())
                    .build()));
    }

    private static void requestOrganizationsEmpty(CloudFoundryClient cloudFoundryClient, String organization) {
        when(cloudFoundryClient.organizations()
            .list(fillPage(ListOrganizationsRequest.builder())
                .name(organization)
                .build()))
            .thenReturn(Mono
                .just(fillPage(ListOrganizationsResponse.builder(), "organization-")
                    .build()));
    }

    private static void requestDomains(CloudFoundryClient cloudFoundryClient, String name, String ownerId) {
        when(cloudFoundryClient.domains()
            .list(fillPage(org.cloudfoundry.client.v2.domains.ListDomainsRequest.builder())
                .name(name)
                .owningOrganizationId(ownerId)
                .page(any(Integer.class))
                .build()))
            .thenReturn(Mono
                .just(fillPage(ListDomainsResponse.builder())
                    .resource(fill(DomainResource.builder(), "domain-")
                        .build())
                    .build()));
    }

    private static void requestDomainsWithNoOwningOrganization(CloudFoundryClient cloudFoundryClient, String name, String ownerId) {
        when(cloudFoundryClient.domains()
            .list(fillPage(org.cloudfoundry.client.v2.domains.ListDomainsRequest.builder())
                .name(name)
                .owningOrganizationId(ownerId)
                .page(any(Integer.class))
                .build()))
            .thenReturn(Mono
                .just(fillPage(ListDomainsResponse.builder())
                    .resource(fill(DomainResource.builder(), "domain-")
                        .entity(DomainEntity.builder()
                            .name(name)
                            .owningOrganizationId(null)
                            .build())
                        .build())
                    .build()));
    }

    public static final class CreateDomain extends AbstractOperationsApiTest<Void> {

        private final DefaultDomains domains = new DefaultDomains(this.cloudFoundryClient);

        @Before
        public void setUp() throws Exception {
            requestOrganizations(this.cloudFoundryClient, "test-organization");
            requestCreateDomain(this.cloudFoundryClient, "test-domain", "test-organization-id");
        }

        @Override
        protected void assertions(TestSubscriber<Void> testSubscriber) {
            // Expects onComplete() with no onNext()
        }

        @Override
        protected Mono<Void> invoke() {
            return this.domains
                .create(CreateDomainRequest.builder()
                    .domain("test-domain")
                    .organization("test-organization")
                    .build());
        }

    }

    public static final class CreateDomainInvalidOrganization extends AbstractOperationsApiTest<Void> {

        private final DefaultDomains domains = new DefaultDomains(this.cloudFoundryClient);

        @Before
        public void setUp() throws Exception {
            requestOrganizationsEmpty(this.cloudFoundryClient, "test-organization");
        }

        @Override
        protected void assertions(TestSubscriber<Void> testSubscriber) {
            testSubscriber
                .assertError(IllegalArgumentException.class, "Organization test-organization does not exist");
        }

        @Override
        protected Mono<Void> invoke() {
            return this.domains
                .create(CreateDomainRequest.builder()
                    .domain("test-domain")
                    .organization("test-organization")
                    .build());
        }

    }

    public static final class CreateSharedDomain extends AbstractOperationsApiTest<Void> {

        private final DefaultDomains domains = new DefaultDomains(this.cloudFoundryClient);

        @Before
        public void setUp() throws Exception {
            requestCreateSharedDomain(this.cloudFoundryClient, "test-domain");
        }

        @Override
        protected void assertions(TestSubscriber<Void> testSubscriber) {
            // Expects onComplete() with no onNext()
        }

        @Override
        protected Mono<Void> invoke() {
            return this.domains
                .createShared(CreateSharedDomainRequest.builder()
                    .domain("test-domain")
                    .build());
        }

    }

    public static final class CreateSharedDomainInvalidRequest extends AbstractOperationsApiTest<Void> {

        private final DefaultDomains domains = new DefaultDomains(this.cloudFoundryClient);

        @Override
        protected void assertions(TestSubscriber<Void> testSubscriber) {
            testSubscriber
                .assertError(RequestValidationException.class, "Request is invalid: domain must be specified");
        }

        @Override
        protected Mono<Void> invoke() {
            return this.domains
                .createShared(CreateSharedDomainRequest.builder()
                    .build());
        }

    }

    public static final class ListDomains extends AbstractOperationsApiTest<Domain> {

        private final DefaultDomains domains = new DefaultDomains(this.cloudFoundryClient);

        @Before
        public void setUp() throws Exception {
            requestDomains(this.cloudFoundryClient, "test-domain-name", "test-domain-owningOrganizationId");
        }

        @Override
        protected void assertions(TestSubscriber<Domain> testSubscriber) {
            testSubscriber
                .assertEquals(Domain.builder()
                    .domainName("test-domain-name")
                    .domainId("test-domain-id")
                    .owningOrganizationId("test-domain-owningOrganizationId")
                    .build());
        }

        @Override
        protected Publisher<Domain> invoke() {
            return this.domains
                .list(ListDomainsRequest.builder()
                    .name("test-domain-name")
                    .owningOrganizationId("test-domain-owningOrganizationId")
                    .build());
        }

    }

    public static final class ListDomainsWithNoOwningOrganizationId extends AbstractOperationsApiTest<Domain> {

        private final DefaultDomains domains = new DefaultDomains(this.cloudFoundryClient);

        @Before
        public void setUp() throws Exception {
            requestDomainsWithNoOwningOrganization(this.cloudFoundryClient, "test-domain-name", null);
        }

        @Override
        protected void assertions(TestSubscriber<Domain> testSubscriber) {
            testSubscriber
                .assertEquals(Domain.builder()
                    .domainName("test-domain-name")
                    .domainId("test-domain-id")
                    .build());
        }

        @Override
        protected Publisher<Domain> invoke() {
            return this.domains
                .list(ListDomainsRequest.builder()
                    .name("test-domain-name")
                    .build());
        }

    }

}
