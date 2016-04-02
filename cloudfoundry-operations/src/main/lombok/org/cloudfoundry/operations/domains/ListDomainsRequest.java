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

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import org.cloudfoundry.Validatable;
import org.cloudfoundry.ValidationResult;
import org.cloudfoundry.client.v2.InFilterParameter;
import org.cloudfoundry.client.v2.PaginatedRequest;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class ListDomainsRequest extends PaginatedRequest implements Validatable {

    /**
     * The domain names
     *
     * @param names the domain names
     * @return the domain names
     */
    @Getter(onMethod = @__(@InFilterParameter("name")))
    private final List<String> names;

    /**
     * The owning organization id
     *
     * @param owningOrganizationIds the owning organization ids
     * @return the owning organization ids
     */
    @Getter(onMethod = @__(@InFilterParameter("owning_organization_guid")))
    private final List<String> owningOrganizationIds;

    @Builder
    ListDomainsRequest(OrderDirection orderDirection, Integer page, Integer resultsPerPage,
                       @Singular List<String> names,
                       @Singular List<String> owningOrganizationIds) {
        super(orderDirection, page, resultsPerPage);
        this.names = names;
        this.owningOrganizationIds = owningOrganizationIds;
    }

    @Override
    public ValidationResult isValid() {
        return ValidationResult.builder().build();
    }
}
