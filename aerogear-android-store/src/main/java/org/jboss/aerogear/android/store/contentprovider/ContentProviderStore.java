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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.core.ReadFilter;
import org.jboss.aerogear.android.core.reflection.Scan;
import org.jboss.aerogear.android.store.DataManager;
import org.jboss.aerogear.android.store.Store;
import org.jboss.aerogear.android.store.sql.SQLStore;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * This class wraps access to a Store in a ContentProvider.
 *
 */
public abstract class ContentProviderStore extends ContentProvider {

    private static final String TAG = ContentProvider.class.getSimpleName();
    private static final int DEFAULT_TIMEOUT_SECONDS = 5; //5 seconds.

    private final Map<Uri, Store<?>> storeMap = new HashMap<Uri, Store<?>>();

    /**
     * SQLStores are opened asynchronously. The latches used to require stores
     * to be open before the are used are referenced here. They are mapped by
     * the Uris the store supports of the Store.
     */
    private final Map<Uri, CountDownLatch> latchMap = new HashMap<Uri, CountDownLatch>();

    /**
     * Implementations are responsible for deserializing the object represtened
     * by the parameters
     *
     * @param uri the URI for the object
     * @param values the Content values provided to the content provider.
     *
     * @return the deserialized object
     *
     */
    abstract Object getObject(Uri uri, ContentValues values);

    /**
     * The implementation is responsible for turning an Object into content
     * values.
     *
     * @param uri the uri of the object being valueized
     * @param value the object to turn into content values
     * @return a valueized version of the object
     */
    abstract ContentValues getValues(Uri uri, Object value);

    
    /**
     * 
     * This method should setup all stores necessary and return a map of 
     * StoreNames (must match the names in DataManager) to URI's the Store can 
     * handle.
     * 
     * @return a map of storeNames to handled uris;
     */
    abstract Map<String, List<Uri>> buildStoreMap();

    
    /**
     * The implementation is responsible for mutating the given object by
     * setting the given values. This method is called in response to an UPDATE.
     *
     * @param uri the uri of the object
     * @param values the values to set on the object
     * @param value the object to mutate
     */
    abstract void merge(Uri uri, ContentValues values, Object value);

    @Override
    public boolean onCreate() {
        Map<String, List<Uri>> uriStores = buildStoreMap();
        for (String storeName : uriStores.keySet()) {
            addStore(storeName, uriStores.get(storeName));
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Store store = getStore(uri);
        List results = new ArrayList();
        if (selection == null || selection.isEmpty()) {
            results.addAll(store.readAll());
        } else {
            ReadFilter filter = makeFilter(selection, selectionArgs);
            results.addAll(store.readWithFilter(filter));
        }
            
        //TODO: Future me figure out how to turn objects into a cursor using getValues
            
    }

    /**
     * Implementations should override this method to provide useful return
     * values.
     *
     * @param uri an uri
     * @return the constant "application/octet-stream"
     */
    @Override
    public String getType(Uri uri) {
        return "application/octet-stream";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Store store = getStore(uri);

        Object value = getObject(uri, values);
        store.save(value);

        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
        Store store = getStore(uri);
        List valuesList = new ArrayList(valuesArray.length);

        for (ContentValues values : valuesArray) {
            Object value = getObject(uri, values);
            valuesList.add(value);
        }

        store.save(valuesList);
        return valuesArray.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Store store = getStore(uri);

        if (selectionArgs == null || selectionArgs[0] == null) {
            store.reset();
        }

        List valueList = store.readWithFilter(makeFilter(selection, selectionArgs));
        if (valueList != null && valueList.size() > 0) {
            for (Object value : valueList) {
                String id = Scan.findIdValueIn(value);
                store.remove(id);
            }
        }

        return 1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Store store = getStore(uri);
        List valueList = store.readWithFilter(makeFilter(selection, selectionArgs));

        if (valueList != null && valueList.size() > 0) {
            for (Object value : valueList) {
                merge(uri, values, value);
            }
        }

        store.save(valueList);

        return 1;

    }

    /**
     * @param storeName this is the name of the store in the DataManager.
     * @param uris a collection of Uris the Store will handle.
     *
     * @throws IllegalArgumentException if storeName is not a created store.
     */
    private final void addStore(String storeName, List<Uri> uris) {
        Store store = DataManager.getStore(storeName);

        if (store == null) {
            throw new IllegalArgumentException(storeName + " is not a store in DataManager");
        }

        if (store instanceof SQLStore) {
            ((SQLStore) store).open(new CountdownLatchCallback(addLatch(uris)));
        }

        for (Uri uri : uris) {
            storeMap.put(uri, store);
        }

    }

    private CountDownLatch addLatch(List<Uri> uris) {
        CountDownLatch latch = new CountDownLatch(1);
        for (Uri uri : uris) {
            latchMap.put(uri, latch);
        }
        return latch;
    }

    /**
     * Ensures that the store is open, exists, etc.
     *
     * For SQLStores if a store is not open it will block until the store is
     * available or a timeout is reached.
     *
     * @param uri the URI for the store being tested
     * @param store the store being tested.
     */
    private void ensureOpen(Uri uri, Store store) {
        CountDownLatch latch = latchMap.get(uri);

        if (store == null) {
            String errorMessage = String.format("Store %s does not exist", uri.toString());
            Log.e(TAG, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        if (latch != null) {
            try {
                if (!latch.await(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    String errorMessage = String.format("Latch %s timed out opening", uri.toString());
                    Log.e(TAG, errorMessage);
                    throw new IllegalStateException(errorMessage);
                }
            } catch (InterruptedException ex) {
                Log.e(TAG, ex.getMessage(), ex);
            }

        }
    }

    private Store getStore(Uri uri) {
        Store<?> store = storeMap.get(uri);
        ensureOpen(uri, store);
        return store;
    }

    private ReadFilter makeFilter(String selection, String[] selectionArgs) {
        ReadFilter filter = new ReadFilter();
        StringBuilder whereBuilder = new StringBuilder(selection.length() + selectionArgs.length * 10);//estimating a reasonable initial size
        String[] whereArgs = selection.split("\\?");
        for (int i = 0; i < whereArgs.length; i++) {
            whereBuilder.append(whereArgs[i]).append(JSONObject.quote(selectionArgs[i]));
        }
        try {
            filter.setWhere(new JSONObject(whereBuilder.toString()));
        } catch (JSONException ex) {
            Log.e(TAG, ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        return filter;
    }

    private static final class CountdownLatchCallback implements Callback {

        private final CountDownLatch latch;

        public CountdownLatchCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void onSuccess(Object data) {
            latch.countDown();
        }

        @Override
        public void onFailure(Exception e) {
            Log.e(TAG, e.getMessage(), e);
            latch.countDown();
        }
    }

}
