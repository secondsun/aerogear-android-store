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
import android.util.Pair;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.lang.reflect.Field;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.aerogear.android.store.Store;

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
    public final Object getObject(Uri uri, ContentValues values) {
        Gson gson = uriGsonMap.get(uri);
        Pair<Class<?>, Store<?>> classStore = getStoreClass(uri);
        JsonElement josinifiedValues = buildJsonObject(values);
        return gson.fromJson(josinifiedValues, classStore.first);
    }

    @Override
    protected final ContentValues getValues(Uri uri, Object value) {
        Gson gson = uriGsonMap.get(uri);
        ContentValues asValues = new ContentValues();
        JsonElement jsonSerialized = gson.toJsonTree(value);
        buildValues(jsonSerialized, "", asValues);
        return asValues;
    }

    @Override
    protected final void merge(Uri uri, ContentValues values, Object value) {
        for (String key : values.keySet()) {
            try {
                setValue(value, key, values.get(key));
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(GsonAwareContentProviderStore.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(GsonAwareContentProviderStore.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(GsonAwareContentProviderStore.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * This will map URIs to Gson objects that can decode and encode objects of
     * the type specified by the URI
     *
     * @return a map of URI objects and their GSonHandler;
     */
    protected abstract Map<Uri, Gson> buildGsonMap();

    private JsonElement buildJsonObject(ContentValues values) {
        JsonObject toReturn = new JsonObject();
        
        SortedSet<String> valueKeys = new TreeSet<String>(values.keySet());
        Iterator<String> keysIterator = valueKeys.iterator();
        while(keysIterator.hasNext()) {
            String key = keysIterator.next();
            if (key.contains(".")) {
                List<String> path = asList(key.split("."));
                Iterator<String> pathIterator = path.iterator();
                JsonObject pathObject = toReturn;
                while (pathIterator.hasNext()) {
                    String pathKey = pathIterator.next();
                    if (pathIterator.hasNext()) {
                        pathObject = pathObject.getAsJsonObject(pathKey);
                    } else {
                        pathObject.addProperty(pathKey, values.getAsString(key));
                    }
                }
            } else {
                toReturn.addProperty(key, values.getAsString(key));
            }
        }
        
        return toReturn;
        
    }

    public static void buildValues(JsonElement serialized, String path, ContentValues values) {
        
        
        if (serialized.isJsonObject()) {
            Set<Map.Entry<String, JsonElement>> members = ((JsonObject) serialized).entrySet();
            String pathVar = path.isEmpty() ? "" : ".";

            for (Map.Entry<String, JsonElement> member : members) {
                JsonElement jsonValue = member.getValue();
                String propertyName = member.getKey();

                if (jsonValue.isJsonArray()) {
                    throw new IllegalArgumentException("Collections are not supported");
                } else {
                    buildValues(jsonValue, path + pathVar + propertyName, values);
                }
            }
        } else if (serialized.isJsonPrimitive()) {
            JsonPrimitive primitive = serialized.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                String value = primitive.getAsBoolean() ? "true" : "false";
                values.put(path, value);
            } else if (primitive.isNumber()) {
                String value = primitive.getAsString();
                values.put(path, value);
            } else if (primitive.isString()) {
                String value = primitive.getAsString();
                values.put(path, value);
            } else {
                throw new IllegalArgumentException(serialized + " isn't a number, boolean, or string");
            }
        } else {
            throw new IllegalArgumentException(serialized + " isn't a JsonObject or JsonPrimitive");
        }
    }

    private void setValue(Object sourceObject, String fieldName, Object newValue) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (fieldName.contains(".")) {
            final String[] parts = fieldName.split("\\.", 1);
            Field field = sourceObject.getClass().getDeclaredField(parts[0]);
            field.setAccessible(true);
            Object fieldValue = field.get(sourceObject);
            setValue(fieldValue, parts[1], newValue);
        } else {
            Field field = sourceObject.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Class type = field.getType();
            if (type.isPrimitive()) {
                if (type.equals(int.class)) {
                    field.setInt(sourceObject, Integer.valueOf(newValue.toString()));
                } else if (type.equals(float.class)) {
                    field.setFloat(sourceObject, Float.valueOf(newValue.toString()));
                } else if (type.equals(boolean.class)) {
                    field.setBoolean(sourceObject, Boolean.valueOf(newValue.toString()));
                } else if (type.equals(byte.class)) {
                    field.setByte(sourceObject, Byte.valueOf(newValue.toString()));
                } else if (type.equals(char.class)) {
                    field.setChar(sourceObject, newValue.toString().charAt(0));
                } else if (type.equals(double.class)) {
                    field.setDouble(sourceObject, Double.valueOf(newValue.toString()));
                } else if (type.equals(long.class)) {
                    field.setLong(sourceObject, Long.valueOf(newValue.toString()));
                } else {
                    field.setShort(sourceObject, Short.valueOf(newValue.toString()));
                }
            } else {
                field.set(sourceObject, newValue);
            }
        }
    }
    
    
}
