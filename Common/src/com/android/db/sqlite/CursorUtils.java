/**
 * Copyright (c) 2012-2013, Michael Yang 鏉ㄧ娴� (www.yangfuhai.com).
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
package com.android.db.sqlite;

import android.database.Cursor;

import com.android.db.CFrameDb;
import com.android.db.table.ManyToOne;
import com.android.db.table.OneToMany;
import com.android.db.table.Property;
import com.android.db.table.TableInfo;

import java.util.HashMap;
import java.util.Map.Entry;


public class CursorUtils {

    public static <T> T getEntity(Cursor cursor, Class<T> clazz, CFrameDb db) {
        try {
            if (cursor != null) {
                TableInfo table = TableInfo.get(clazz);
                int columnCount = cursor.getColumnCount();
                if (columnCount > 0) {
                    T entity = (T) clazz.newInstance();
                    for (int i = 0; i < columnCount; i++) {

                        String column = cursor.getColumnName(i);

                        Property property = table.propertyMap.get(column);
                        if (property != null) {
                            property.setValue(entity, cursor.getString(i));
                        } else {
                            if (table.getId().getColumn().equals(column)) {
                                table.getId().setValue(entity, cursor.getString(i));
                            }
                        }

                    }
                    /**
                     * 澶勭悊OneToMany鐨刲azyLoad褰㈠紡
                     */
                    for (OneToMany oneToManyProp : table.oneToManyMap.values()) {
                        if (oneToManyProp.getDataType() == OneToManyLazyLoader.class) {
                            OneToManyLazyLoader oneToManyLazyLoader = new OneToManyLazyLoader(entity, clazz, oneToManyProp.getOneClass(), db);
                            oneToManyProp.setValue(entity, oneToManyLazyLoader);
                        }
                    }

                    /**
                     * 澶勭悊ManyToOne鐨刲azyLoad褰㈠紡
                     */
                    for (ManyToOne manyToOneProp : table.manyToOneMap.values()) {
                        if (manyToOneProp.getDataType() == ManyToOneLazyLoader.class) {
                            ManyToOneLazyLoader manyToOneLazyLoader = new ManyToOneLazyLoader(entity, clazz, manyToOneProp.getManyClass(), db);
                            manyToOneLazyLoader.setFieldValue(cursor.getInt(cursor.getColumnIndex(manyToOneProp.getColumn())));
                            manyToOneProp.setValue(entity, manyToOneLazyLoader);
                        }
                    }
                    return entity;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static DbModel getDbModel(Cursor cursor) {
        if (cursor != null && cursor.getColumnCount() > 0) {
            DbModel model = new DbModel();
            int columnCount = cursor.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                model.set(cursor.getColumnName(i), cursor.getString(i));
            }
            return model;
        }
        return null;
    }


    public static <T> T dbModel2Entity(DbModel dbModel, Class<?> clazz) {
        if (dbModel != null) {
            HashMap<String, Object> dataMap = dbModel.getDataMap();
            try {
                @SuppressWarnings("unchecked")
                T entity = (T) clazz.newInstance();
                for (Entry<String, Object> entry : dataMap.entrySet()) {
                    String column = entry.getKey();
                    TableInfo table = TableInfo.get(clazz);
                    Property property = table.propertyMap.get(column);
                    if (property != null) {
                        property.setValue(entity, entry.getValue() == null ? null : entry.getValue().toString());
                    } else {
                        if (table.getId().getColumn().equals(column)) {
                            table.getId().setValue(entity, entry.getValue() == null ? null : entry.getValue().toString());
                        }
                    }

                }
                return entity;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }


}
