/*
 * Copyright 2015 JBoss by Red Hat.
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
package org.jboss.aerogear.android.store.test.contentprovider;

import android.net.Uri;
import android.util.Pair;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.aerogear.android.store.DataManager;
import org.jboss.aerogear.android.store.contentprovider.GsonAwareContentProviderStore;
import org.jboss.aerogear.android.store.sql.SQLStore;
import org.jboss.aerogear.android.store.sql.SQLStoreConfiguration;
import org.jboss.aerogear.android.store.test.helper.Data;

public class DataContentProvider extends GsonAwareContentProviderStore {

    public static final Uri DATA_URI = Uri.parse("content://org.jboss.aerogear.android.store.test/Data");
    
    @Override
    protected Map<Uri, Gson> buildGsonMap() {
        Map<Uri, Gson> toReturn = new HashMap<Uri, Gson>();
        toReturn.put(DATA_URI, new Gson());
        return toReturn;
    }

    @Override
    protected Map<String, List<Pair<Uri, Class<?>>>> buildStoreMap() {
        Map<String, List<Pair<Uri, Class<?>>>> storeNameMap = new HashMap<String, List<Pair<Uri, Class<?>>>>();
        Pair<Uri, Class<Data>> uriClass = Pair.create(DATA_URI, Data.class);
        List uriClassList = new ArrayList(1);
        uriClassList.add(uriClass);
        
        SQLStore store = (SQLStore) DataManager
                .config("contentProviderStore", SQLStoreConfiguration.class)
                .forClass(Data.class)
                .withContext(getContext())
                .store();
        
        storeNameMap.put("contentProviderStore", uriClassList);
        
        return storeNameMap;
    }
    
}
