package com.android.db.sqlite;


import com.android.db.CFrameDb;

/**
 * 涓�瀵瑰寤惰繜鍔犺浇绫�
 * Created by cxd on 13-7-25.
 *
 * @param <O> 瀹夸富瀹炰綋鐨刢lass
 * @param <M> 澶氭斁瀹炰綋class
 */
public class ManyToOneLazyLoader<M, O> {
    M manyEntity;
    Class<M> manyClazz;
    Class<O> oneClazz;
    CFrameDb db;
    /**
     * 鐢ㄤ簬
     */
    private Object fieldValue;

    public ManyToOneLazyLoader(M manyEntity, Class<M> manyClazz, Class<O> oneClazz, CFrameDb db) {
        this.manyEntity = manyEntity;
        this.manyClazz = manyClazz;
        this.oneClazz = oneClazz;
        this.db = db;
    }

    O oneEntity;
    boolean hasLoaded = false;

    /**
     * 濡傛灉鏁版嵁鏈姞杞斤紝鍒欒皟鐢╨oadManyToOne濉厖鏁版嵁
     *
     * @return
     */
    public O get() {
        if (oneEntity == null && !hasLoaded) {
            this.db.loadManyToOne(null, this.manyEntity, this.manyClazz, this.oneClazz);
            hasLoaded = true;
        }
        return oneEntity;
    }

    public void set(O value) {
        oneEntity = value;
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
    }
}
