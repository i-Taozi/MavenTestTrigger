package com.ontology2.bakemono.joins;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VIntWritable;

public class TextSimpleJoinMapper extends SetJoinMapper<Text> {
    @Override
    TaggedItem<Text> newTaggedKey(Text key, VIntWritable tag) {
        return new TaggedTextItem(key,tag);
    }
}
