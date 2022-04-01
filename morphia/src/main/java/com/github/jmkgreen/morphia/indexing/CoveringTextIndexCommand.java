package com.github.jmkgreen.morphia.indexing;

import com.github.jmkgreen.morphia.utils.IndexDirection;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import java.util.Map;

/**
 * A way of creating an index to allow a covering query using a text search.
 */
public class CoveringTextIndexCommand extends TextIndexCommand {

    private Map<String, IndexDirection> coveringFields;

    public void setCoveringFields(Map<String, IndexDirection> coveringFields) {
        this.coveringFields = coveringFields;
    }

    public DBObject getKeys() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start(super.getKeys().toMap());

        for (Map.Entry<String, IndexDirection> entry : coveringFields.entrySet()) {
            builder.add(entry.getKey(), entry.getValue().toIndexValue());
        }

        return builder.get();
    }
}
