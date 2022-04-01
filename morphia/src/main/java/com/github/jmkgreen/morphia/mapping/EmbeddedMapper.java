/**
 *
 */
package com.github.jmkgreen.morphia.mapping;

import com.github.jmkgreen.morphia.mapping.cache.EntityCache;
import com.github.jmkgreen.morphia.utils.IterHelper;
import com.github.jmkgreen.morphia.utils.IterHelper.MapIterCallback;
import com.github.jmkgreen.morphia.utils.ReflectionUtils;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "rawtypes"})
class EmbeddedMapper implements CustomMapper {
    public void toDBObject(final Object entity, final MappedField mf, final DBObject dbObject, Map<Object, DBObject> involvedObjects, Mapper mapr) {
        String name = mf.getNameToStore();

        Object fieldValue = mf.getFieldValue(entity);

        if (mf.isMap()) {
            writeMap(mf, dbObject, involvedObjects, name, fieldValue, mapr);
        } else if (mf.isMultipleValues()) {
            writeCollection(mf, dbObject, involvedObjects, name, fieldValue, mapr);
        } else {
            //run converters
            if (mapr.getConverters().hasDbObjectConverter(mf) || mapr.getConverters().hasDbObjectConverter(entity.getClass())) {
                mapr.getConverters().toDBObject(entity, mf, dbObject, mapr.getOptions());
                return;
            }

            DBObject dbObj = fieldValue == null ? null : mapr.toDBObject(fieldValue, involvedObjects);
            if (dbObj != null) {
                if (!shouldSaveClassName(fieldValue, dbObj, mf))
                    dbObj.removeField(Mapper.CLASS_NAME_FIELDNAME);

                if (dbObj.keySet().size() > 0 || mapr.getOptions().storeEmpties) {
                    dbObject.put(name, dbObj);
                }
            }
        }
    }

    private void writeCollection(final MappedField mf, final DBObject dbObject, Map<Object, DBObject> involvedObjects, String name, Object fieldValue, Mapper mapr) {
        Iterable coll = null;

        if (fieldValue != null)
            if (mf.isArray)
                coll = Arrays.asList((Object[]) fieldValue);
            else
                coll = (Iterable) fieldValue;

        if (coll != null) {
            List values = new ArrayList();
            for (Object o : coll) {
                if (null == o)
                    values.add(null);
                else if (mapr.getConverters().hasSimpleValueConverter(mf) || mapr.getConverters().hasSimpleValueConverter(o.getClass()))
                    values.add(mapr.getConverters().encode(o));
                else {
                    Object val;
                    if (Collection.class.isAssignableFrom(o.getClass()) || Map.class.isAssignableFrom(o.getClass()))
                        val = mapr.toMongoObject(o, true);
                    else
                        val = mapr.toDBObject(o, involvedObjects);

                    if (!shouldSaveClassName(o, val, mf))
                        ((DBObject) val).removeField(Mapper.CLASS_NAME_FIELDNAME);

                    values.add(val);
                }
            }
            if (values.size() > 0 || mapr.getOptions().storeEmpties) {
                dbObject.put(name, values);
            }
        }
    }

    private void writeMap(final MappedField mf, final DBObject dbObject, Map<Object, DBObject> involvedObjects, String name, Object fieldValue, Mapper mapr) {
        Map<String, Object> map = (Map<String, Object>) fieldValue;
        if (map != null) {
            BasicDBObject values = new BasicDBObject();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object entryVal = entry.getValue();
                Object val;

                if (entryVal == null)
                    val = null;
                else if (mapr.getConverters().hasSimpleValueConverter(mf) || mapr.getConverters().hasSimpleValueConverter(entryVal.getClass()))
                    val = mapr.getConverters().encode(entryVal);
                else {
                    if (Map.class.isAssignableFrom(entryVal.getClass()) || Collection.class.isAssignableFrom(entryVal.getClass()))
                        val = mapr.toMongoObject(entryVal, true);
                    else
                        val = mapr.toDBObject(entryVal, involvedObjects);

                    if (!shouldSaveClassName(entryVal, val, mf))
                        ((DBObject) val).removeField(Mapper.CLASS_NAME_FIELDNAME);
                }

                String strKey = mapr.getConverters().encode(entry.getKey()).toString();
                values.put(strKey, val);
            }

            if (values.size() > 0 || mapr.getOptions().storeEmpties)
                dbObject.put(name, values);
        }
    }

    public void fromDBObject(final DBObject dbObject, final MappedField mf, final Object entity, EntityCache cache, Mapper mapr) {
        try {
            if (mf.isMap()) {
                readMap(dbObject, mf, entity, cache, mapr);
            } else if (mf.isMultipleValues()) {
                readCollection(dbObject, mf, entity, cache, mapr);
            } else {
                // single element

                Object dbVal = mf.getDbObjectValue(dbObject);
                if (dbVal != null) {
                    boolean isDBObject = dbVal instanceof DBObject && !(dbVal instanceof BasicDBList);

                    //run converters
                    if (isDBObject && (mapr.getConverters().hasDbObjectConverter(mf) || mapr.getConverters().hasDbObjectConverter(mf.getType()))) {
                        mapr.getConverters().fromDBObject(((DBObject) dbVal), mf, entity);
                        return;
                    } else {
                        Object refObj = null;
                        if (mapr.getConverters().hasSimpleValueConverter(mf) || mapr.getConverters().hasSimpleValueConverter(mf.getType()))
                            refObj = mapr.getConverters().decode(mf.getType(), dbVal, mf);
                        else {
                            refObj = mapr.getOptions().objectFactory.createInstance(mapr, mf, ((DBObject) dbVal));
                            refObj = mapr.fromDBObject(((DBObject) dbVal), refObj, cache);
                        }
                        if (refObj != null) {
                            mf.setFieldValue(entity, refObj);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void readCollection(final DBObject dbObject, final MappedField mf, final Object entity, EntityCache cache, Mapper mapr) {
        // multiple documents in a List
        Collection values = mf.isSet() ? mapr.getOptions().objectFactory.createSet(mf) : mapr.getOptions().objectFactory.createList(mf);

        Object dbVal = mf.getDbObjectValue(dbObject);
        if (dbVal != null) {

            List dbVals = null;
            if (dbVal instanceof List)
                dbVals = (List) dbVal;
            else {
                dbVals = new BasicDBList();
                dbVals.add(dbVal);
            }

            for (Object o : dbVals) {

                DBObject dbObj = (DBObject) o;
                Object newEntity = null;

                if (dbObj != null) {
                    //run converters
                    if (mapr.getConverters().hasSimpleValueConverter(mf) || mapr.getConverters().hasSimpleValueConverter(mf.getSubClass()))
                        newEntity = mapr.getConverters().decode(mf.getSubClass(), dbObj, mf);
                    else {
                        newEntity = readMapOrCollectionOrEntity(dbObj, mf, cache, mapr);
                    }
                }

                values.add(newEntity);
            }
        }
        if (values.size() > 0) {
            if (mf.getType().isArray()) {
                mf.setFieldValue(entity, ReflectionUtils.convertToArray(mf.getSubClass(), ReflectionUtils.iterToList(values)));
            } else {
                mf.setFieldValue(entity, values);
            }
        }
    }

    private void readMap(final DBObject dbObject, final MappedField mf, final Object entity, final EntityCache cache, final Mapper mapr) {
        final Map map = mapr.getOptions().objectFactory.createMap(mf);

        DBObject dbObj = (DBObject) mf.getDbObjectValue(dbObject);
        new IterHelper<Object, Object>().loopMap((Object) dbObj, new MapIterCallback<Object, Object>() {
            @Override
            public void eval(Object key, Object val) {
                Object newEntity = null;

                //run converters
                if (val != null) {
                    if (mapr.getConverters().hasSimpleValueConverter(mf) ||
                            mapr.getConverters().hasSimpleValueConverter(mf.getSubClass()))
                        newEntity = mapr.getConverters().decode(mf.getSubClass(), val, mf);
                    else {
                        if (val instanceof DBObject)
                            newEntity = readMapOrCollectionOrEntity((DBObject) val, mf, cache, mapr);
                        else
                            throw new MappingException("Embedded element isn't a DBObject! How can it be that is a " + val.getClass());

                    }
                }

                Object objKey = mapr.getConverters().decode(mf.getMapKeyClass(), key);
                map.put(objKey, newEntity);
            }
        });

        if (map.size() > 0) {
            mf.setFieldValue(entity, map);
        }
    }

    private Object readMapOrCollectionOrEntity(DBObject dbObj, MappedField mf, EntityCache cache, Mapper mapr) {
        if (Map.class.isAssignableFrom(mf.getSubClass()) || Iterable.class.isAssignableFrom(mf.getSubClass())) {
            MapOrCollectionMF mocMF = new MapOrCollectionMF((ParameterizedType) mf.getSubType());
            mapr.fromDBObject(dbObj, mocMF, cache);
            return mocMF.getValue();
        } else {
            Object newEntity = mapr.getOptions().objectFactory.createInstance(mapr, mf, dbObj);
            return mapr.fromDBObject(dbObj, newEntity, cache);
        }
    }

    public static boolean shouldSaveClassName(Object rawVal, Object convertedVal, MappedField mf) {
        if (rawVal == null || mf == null)
            return true;
        if (mf.isSingleValue())
            return !(mf.getType().equals(rawVal.getClass()) && !(convertedVal instanceof BasicDBList));
        else if (convertedVal != null &&
                convertedVal instanceof DBObject &&
                !mf.getSubClass().isInterface() &&
                !Modifier.isAbstract(mf.getSubClass().getModifiers()) &&
                mf.getSubClass().equals(rawVal.getClass()))
            return false;
        else
            return true;
    }

}
