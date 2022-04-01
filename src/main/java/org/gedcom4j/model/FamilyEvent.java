/*
 * Copyright (c) 2009-2016 Matthew R. Harrah
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package org.gedcom4j.model;

import org.gedcom4j.model.enumerations.FamilyEventType;

/**
 * Represents a family event. Corresponds to the FAMILY_EVENT_STRUCTURE from the GEDCOM standard along with the two child elements
 * of the wife and husband ages.
 * 
 * @author frizbog1
 * 
 */
public class FamilyEvent extends AbstractEvent {
    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -5964078401105991388L;

    /**
     * Age of husband at time of event
     */
    private StringWithCustomFacts husbandAge;

    /**
     * The type of event. See FAMILY_EVENT_STRUCTURE in the GEDCOM standard for more info.
     */
    private FamilyEventType type;

    /**
     * Age of wife at time of event
     */
    private StringWithCustomFacts wifeAge;

    /** Default constructor */
    public FamilyEvent() {
        // Default constructor does nothing
    }

    /**
     * Copy constructor
     * 
     * @param other
     *            object being copied
     */
    public FamilyEvent(FamilyEvent other) {
        super(other);
        if (other.husbandAge != null) {
            husbandAge = new StringWithCustomFacts(other.husbandAge);
        }
        type = other.type;
        if (other.wifeAge != null) {
            wifeAge = new StringWithCustomFacts(other.wifeAge);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof FamilyEvent)) {
            return false;
        }
        FamilyEvent other = (FamilyEvent) obj;
        if (husbandAge == null) {
            if (other.husbandAge != null) {
                return false;
            }
        } else if (!husbandAge.equals(other.husbandAge)) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        if (wifeAge == null) {
            if (other.wifeAge != null) {
                return false;
            }
        } else if (!wifeAge.equals(other.wifeAge)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the husband's age.
     *
     * @return the husband's age
     */
    public StringWithCustomFacts getHusbandAge() {
        return husbandAge;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public FamilyEventType getType() {
        return type;
    }

    /**
     * Gets the wife's age.
     *
     * @return the wife's age
     */
    public StringWithCustomFacts getWifeAge() {
        return wifeAge;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (husbandAge == null ? 0 : husbandAge.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
        result = prime * result + (wifeAge == null ? 0 : wifeAge.hashCode());
        return result;
    }

    /**
     * Sets the husband's age.
     *
     * @param husbandAge
     *            the new husband's age
     */
    public void setHusbandAge(String husbandAge) {
        this.husbandAge = husbandAge == null ? null : new StringWithCustomFacts(husbandAge);
    }

    /**
     * Sets the husband's age.
     *
     * @param husbandAge
     *            the new husband's age
     */
    public void setHusbandAge(StringWithCustomFacts husbandAge) {
        this.husbandAge = husbandAge;
    }

    /**
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    public void setType(FamilyEventType type) {
        this.type = type;
    }

    /**
     * Sets the wife's age.
     *
     * @param wifeAge
     *            the new wife's age
     */
    public void setWifeAge(String wifeAge) {
        this.wifeAge = wifeAge == null ? null : new StringWithCustomFacts(wifeAge);
    }

    /**
     * Sets the wife's age.
     *
     * @param wifeAge
     *            the new wife's age
     */
    public void setWifeAge(StringWithCustomFacts wifeAge) {
        this.wifeAge = wifeAge;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(128);
        builder.append("FamilyEvent [");
        if (husbandAge != null) {
            builder.append("husbandAge=");
            builder.append(husbandAge);
            builder.append(", ");
        }
        if (type != null) {
            builder.append("type=");
            builder.append(type);
            builder.append(", ");
        }
        if (wifeAge != null) {
            builder.append("wifeAge=");
            builder.append(wifeAge);
            builder.append(", ");
        }
        buildAbstractEventToString(builder);
        builder.append("]");
        return builder.toString();
    }
}
