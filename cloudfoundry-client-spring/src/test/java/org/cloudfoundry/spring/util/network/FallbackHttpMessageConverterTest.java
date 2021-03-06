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

package org.cloudfoundry.spring.util.network;

import org.junit.Test;
import org.springframework.mock.http.MockHttpOutputMessage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class FallbackHttpMessageConverterTest {

    private final FallbackHttpMessageConverter messageConverter = new FallbackHttpMessageConverter();

    @Test
    public void canRead() {
        assertFalse(this.messageConverter.canRead(null));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void readInternal() {
        this.messageConverter.readInternal(null, null);
    }

    @Test
    public void supports() {
        assertTrue(this.messageConverter.supports(Object.class));
    }

    @Test
    public void writeInternal() {
        MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();

        this.messageConverter.writeInternal(null, outputMessage);

        assertEquals(0, outputMessage.getBodyAsBytes().length);
    }

}
