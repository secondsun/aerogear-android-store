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
import android.database.AbstractCursor;
import java.util.ArrayList;
import java.util.List;

/**
 * A Cursor which wraps contentvalues;
 */
public class StoreCursor extends AbstractCursor {

    private final List<ContentValues> objectList = new ArrayList<ContentValues>();
    private final String[] columnNames;

    public StoreCursor(List<ContentValues> data) {
        objectList.addAll(data);
        ContentValues firstObject = objectList.get(0);

        if (firstObject != null) {
            int length = firstObject.keySet().size();
            columnNames = firstObject.keySet().toArray(new String[length]);
        } else {
            columnNames = new String[0];
        }

    }

    @Override
    public int getCount() {
        return objectList.size();
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public String getString(int column) {
        return objectList.get(getPosition()).getAsString(columnNames[column]);
    }

    @Override
    public short getShort(int column) {
        return objectList.get(getPosition()).getAsShort(columnNames[column]);
    }

    @Override
    public int getInt(int column) {
        return objectList.get(getPosition()).getAsInteger(columnNames[column]);
    }

    @Override
    public long getLong(int column) {
        return objectList.get(getPosition()).getAsLong(columnNames[column]);
    }

    @Override
    public float getFloat(int column) {
        return objectList.get(getPosition()).getAsFloat(columnNames[column]);
    }

    @Override
    public double getDouble(int column) {
        return objectList.get(getPosition()).getAsDouble(columnNames[column]);
    }

    @Override
    public boolean isNull(int column) {
        return objectList.get(getPosition()).get(columnNames[column]) == null;
    }

}
