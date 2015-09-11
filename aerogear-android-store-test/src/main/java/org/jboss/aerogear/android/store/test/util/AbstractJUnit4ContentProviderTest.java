/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.android.store.test.util;

import android.content.ContentProvider;
import android.test.ProviderTestCase2;
import org.junit.After;
import org.junit.Before;

/**
 * All tests which use Mockito should extend this class.
 * 
 * In Android 4.3 the dexcache is not set as it was previously and Mockito will
 * throw an Exception. This class sets the dexcache directory correctly during
 * test setup.
 * 
 * @param <T> A Concrete ContentProvider Type
 */
public abstract class AbstractJUnit4ContentProviderTest<T extends ContentProvider> extends ProviderTestCase2<T> {

    public AbstractJUnit4ContentProviderTest(Class<T> klass, String providerAuthority) {
        super(klass, providerAuthority);
        
    }
        
    
    /**
     * Annotated method to call old setUp method
     * @throws java.lang.Exception
     */
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        assertNotNull(getProvider());
    }
    
    
    @After
    @Override
    /**
     * Annotated method to call old tearDown method
     */
    public void tearDown() throws Exception {
        super.tearDown();
    }

    
}