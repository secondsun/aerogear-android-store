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
package org.jboss.aerogear.android.store.contentprovider;

import android.content.ContentValues;
import android.net.Uri;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides some basic functionality to make it easier to write
 * content providers using GSON and SQLStores.
 */
public abstract class GsonAwareContentProviderStore extends ContentProviderStore {

    private Map<Uri, Gson> uriGsonMap = new HashMap<Uri, Gson>();
    public static final String DATA_COLUMN = "_DATA";
    
    @Override
    public boolean onCreate() {
        super.onCreate();
        uriGsonMap = buildGsonMap();
        return true;
    }

    @Override
    final Object getObject(Uri uri, ContentValues values) {
        Gson gson = uriGsonMap.get(uri);
        //TODO: future me, figure out how to get the Class information.
    }

    @Override
    ContentValues getValues(Uri uri, Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void merge(Uri uri, ContentValues values, Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * This will map URIs to Gson objects that can decode and encode objects of
     * the type specified by the URI
     *
     * @return a map of URI objects and their GSonHandler;
     */
    abstract Map<Uri, Gson> buildGsonMap();

}
