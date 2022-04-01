/**
 * 
 */
package com.github.jmkgreen.morphia.ext.entityscanner;

import org.bson.types.ObjectId;

import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;

@Entity
class E {
	@Id
	ObjectId id;
}