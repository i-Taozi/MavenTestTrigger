package com.github.jmkgreen.morphia.indexing;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

import java.util.List;
import java.util.Map;

/**
 * Convenient way to build text index commands.
 *
 */
public class TextIndexCommand {
    private static final String CMD = "text";
    private static final String INDEX_NAME = "name";
    private static final String WILDCARD = "$**";
    private static final String DEFAULT_LANGUAGE = "default_language";
    private static final String LANGUAGE_OVERRIDE = "language_override";
    private static final String WEIGHTS = "weights";

    private List<String> fields;
    private Map<String, Integer> weights;
    private String name;
    private String defaultLanguage;
    private String languageOverride;
    private boolean indexAllFields = false;

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public void setWeights(Map<String, Integer> weights) {
        this.weights = weights;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public void setLanguageOverride(String languageOverride) {
        this.languageOverride = languageOverride;
    }

    public void setIndexAllFields() {
        this.indexAllFields = true;
    }

    public void setIndexSpecificFields() {
        this.indexAllFields = false;
    }

    public DBObject getKeys() {
        BasicDBObjectBuilder builder = new BasicDBObjectBuilder();

        if (indexAllFields) {
            builder.add(WILDCARD, CMD);
        } else {
            addFieldsTo(builder);
        }

        return builder.get();
    }

    public DBObject getOptions() {
        BasicDBObjectBuilder builder = new BasicDBObjectBuilder();

        if (name != null && !name.isEmpty()) {
            builder.add(INDEX_NAME, name);
        }

        if (defaultLanguage != null && !defaultLanguage.isEmpty()) {
            builder.add(DEFAULT_LANGUAGE, defaultLanguage);
        }

        if (languageOverride != null && !languageOverride.isEmpty()) {
            builder.add(LANGUAGE_OVERRIDE, languageOverride);
        }
        if (weights !=  null && !weights.isEmpty()) {
            builder.add(WEIGHTS, weights);
        }

        return builder.get();
    }

    private void addFieldsTo(final BasicDBObjectBuilder builder) {
        if (fields == null) {
            throw new IllegalArgumentException("No fields specified for creating index on");
        }

        for (String field : fields) {
            builder.add(field, CMD);
        }
    }

}
