package com.github.jmkgreen.morphia.mapping;

import com.github.jmkgreen.morphia.mapping.cache.EntityCache;
import com.mongodb.DBObject;
import java.util.Map;

/**
 * Simple mapper that just uses the Mapper.getOptions().converts
 *
 * @author Scott Hernnadez
 */
class ValueMapper implements CustomMapper {
    public void toDBObject(Object entity, MappedField mf, DBObject dbObject, Map<Object, DBObject> involvedObjects, Mapper mapr) {
        try {
            mapr.getConverters().toDBObject(entity, mf, dbObject, mapr.getOptions());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void fromDBObject(DBObject dbObject, MappedField mf, Object entity, EntityCache cache, Mapper mapr) {
        try {
            mapr.getConverters().fromDBObject(dbObject, mf, entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
