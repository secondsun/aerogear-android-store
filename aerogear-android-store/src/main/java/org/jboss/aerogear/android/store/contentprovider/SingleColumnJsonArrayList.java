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


import android.database.AbstractCursor;

import com.google.gson.Gson;

import java.util.List;

/**
 * Wraps a GSON serializable value into a single ArrayList and exposes 
 * it via a cursor.
 * 
 * @author summers
 * @param <T> 
 */
public class SingleColumnJsonArrayList<T> extends AbstractCursor {

    private static final String[] COLUMNS = {"DATA", "NOTIFY"};
    private final List<T> cursorList;
    private final Gson gson;

    public SingleColumnJsonArrayList(List<T> cursorList) {
        this.cursorList = cursorList;
        this.gson = new Gson();
    }

    public SingleColumnJsonArrayList(List<T> cursorList, Gson gson) {
        this.cursorList = cursorList;
        this.gson = gson;
    }
    
    @Override
    public int getCount() {
        return cursorList.size();
    }

    @Override
    public String[] getColumnNames() {
        return COLUMNS;
    }

    @Override
    public String getString(int column) {
        return gson.toJson(cursorList.get(super.getPosition()));
    }

    @Override
    public short getShort(int column) {
        return 0;
    }

    @Override
    public int getInt(int column) {
        return 0;
    }

    @Override
    public long getLong(int column) {
        return 0;
    }

    @Override
    public float getFloat(int column) {
        return 0;
    }

    @Override
    public double getDouble(int column) {
        return 0;
    }

    @Override
    public boolean isNull(int column) {
        return false;
    }

}