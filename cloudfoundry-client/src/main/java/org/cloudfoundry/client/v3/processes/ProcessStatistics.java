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

package org.cloudfoundry.client.v3.processes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Singular;

import java.util.List;

/**
 * Process details statistics
 */
public abstract class ProcessStatistics {

    /**
     * The disk quota
     */
    @JsonProperty("disk_quota")
    public abstract Long getDiskQuota();

    /**
     * The file desriptor quota
     */
    @JsonProperty("fds_quota")
    public abstract Long getFdsQuota();

    /**
     * The host
     */
    @JsonProperty("host")
    public abstract String getHost();

    /**
     * The index
     */
    @JsonProperty("index")
    public abstract Integer getIndex();

    /**
     * The instance port mappings
     */
    @JsonProperty("instance_ports")
    public abstract List<PortMapping> getInstancePorts();

    /**
     * The memory quota
     */
    @JsonProperty("mem_quota")
    public abstract Long getMemoryQuota();

    /**
     * The state
     */
    @JsonProperty("state")
    public abstract String getState();

    /**
     * The type
     */
    @JsonProperty("type")
    public abstract String getType();

    /**
     * The uptime
     */
    @JsonProperty("uptime")
    public abstract Long getUptime();

    /**
     * The usage
     */
    @JsonProperty("usage")
    public abstract ProcessUsage getUsage();

}

