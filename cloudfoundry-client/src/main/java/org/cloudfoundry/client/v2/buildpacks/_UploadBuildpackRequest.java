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

package org.cloudfoundry.client.v2.buildpacks;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.immutables.value.Value;

import java.io.InputStream;

/**
 * The request payload to Upload a Buildpack
 */
@Value.Immutable
abstract class _UploadBuildpackRequest {

    /**
     * A binary zip file containing the buildpack bits.
     */
    @JsonIgnore
    abstract InputStream getBuildpack();

    /**
     * The buildpack id
     */
    @JsonIgnore
    abstract String getBuildpackId();

    /**
     * The filename
     */
    @JsonIgnore
    abstract String getFilename();

}
