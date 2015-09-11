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

import android.content.ContentValues;
import android.database.Cursor;
import android.support.test.runner.AndroidJUnit4;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.List;
import org.jboss.aerogear.android.store.contentprovider.GsonAwareContentProviderStore;
import org.jboss.aerogear.android.store.test.helper.Data;
import org.jboss.aerogear.android.store.test.util.AbstractJUnit4ContentProviderTest;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ContentProviderStoreTest  extends AbstractJUnit4ContentProviderTest<DataContentProvider> {

    public ContentProviderStoreTest() {
        super(DataContentProvider.class, "org.jboss.aerogear.android.store.test");
    }
    
    
    @Test
    public void testSave() {
        Data data = new Data(10, "name", "description");
        ContentValues values = new ContentValues();
        GsonAwareContentProviderStore.buildValues(new Gson().toJsonTree(data), "", values);
        getMockContentResolver().insert(DataContentProvider.DATA_URI, values);
        Cursor cursor = getMockContentResolver().query(DataContentProvider.DATA_URI, new String[0], "", new String[0], "");
        assertTrue(cursor.moveToFirst());
        List<String> columnNames = Arrays.asList(cursor.getColumnNames());
        assertEquals(10, cursor.getInt(columnNames.indexOf("id")));
        assertEquals("name", cursor.getInt(columnNames.indexOf("name")));
        assertEquals("description", cursor.getInt(columnNames.indexOf("description")));
    }
}
