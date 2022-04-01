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
package org.gedcom4j.model.thirdpartyadapters;

import java.util.ArrayList;
import java.util.List;

import org.gedcom4j.model.AbstractElement;
import org.gedcom4j.model.CustomFact;
import org.gedcom4j.model.HasCustomFacts;

/**
 * Base class for third party adapter classes
 * 
 * @author frizbog
 */
public abstract class AbstractThirdPartyAdapter {

    /**
     * Clear custom facts of a specific type.
     *
     * @param hct
     *            the object that has custom facts
     * @param tag
     *            the tag type to clear from the custom facts.
     * @return the number of facts removed
     */
    protected int clearCustomTagsOfType(HasCustomFacts hct, String tag) {
        int result = 0;
        List<CustomFact> customFacts = hct.getCustomFacts();
        if (customFacts != null) {
            for (int i = 0; i < customFacts.size();) {
                CustomFact cf = customFacts.get(i);
                if (tag.equals(cf.getTag())) {
                    customFacts.remove(i);
                    result++;
                } else {
                    i++;
                }
            }
        }
        return result;
    }

    /**
     * Clear custom facts of a specific type and subtype. Searches the supplied element <code>elem</code> for custom facts with tag
     * specified in <code>tag</code>, and that also have a subelement of type <code>TYPE</code> that matches the supplied
     * <code>subType</code> value. These matches are removed from the custom facts collection on the element supplied.
     * 
     * @param elem
     *            the object that has custom facts
     * @param tag
     *            the tag type to clear from the custom facts.
     * @param subType
     *            the subtype of item to clear from the custom facts
     * @return the count of items removed
     */
    protected int clearCustomTagsOfTypeAndSubType(AbstractElement elem, String tag, String subType) {
        int result = 0;
        if (elem.getCustomFacts() == null) {
            return result;
        }
        int i = 0;
        while (i < elem.getCustomFacts().size()) {
            CustomFact fact = elem.getCustomFacts().get(i);
            if (fact.getTag().equals(tag) && fact.getType() != null && fact.getType().getValue() != null && fact.getType()
                    .getValue().equals(subType)) {
                elem.getCustomFacts().remove(i);
                result++;
            } else {
                i++;
            }
        }
        return result;
    }

    /**
     * <p>
     * Get a list of custom facts from the supplied element, each of which has a tag that matches the value supplied, and has a
     * sub-element named "TYPE" with the value specified. This is similar to generic EVEN or FACT tags, but also supports other
     * custom tags that follow that convention.
     * </p>
     * <p>
     * Example: given a structure like this:
     * </p>
     * 
     * <pre>
     * 0 @I1@ INDI
     * 1 NAME Edward /Example/
     * 1 _FAVF Pizza
     * 2 TYPE Favorite Food
     * </pre>
     * 
     * a concrete adapter might provide a method like this:
     * 
     * <pre>
     * public List&lt;CustomFact&gt; getFavoriteFoods(Individual ind) {
     *    List&lt;CustomFact&gt; favoriteFoods = getCustomTagsWithTagAndType(ind, "_FAVF", "Favorite Food");
     *    return favoriteFoods;
     * </pre>
     * 
     * and the caller could iterate through the {@link CustomFact#getDescription()} property of each value in the resulting list,
     * where "Pizza" would be found in the list.
     * 
     * 
     * @param elem
     *            the element containing custom facts/tags
     * @param tag
     *            the main tag for the custom fact we're looking for
     * @param type
     *            the type of element we are looking for. Must match a child node of the main custom fact's tag,
     * @return a list of custom tags with the specified tag and (sub)type.
     */
    protected List<CustomFact> getCustomTagsWithTagAndType(AbstractElement elem, String tag, String type) {
        List<CustomFact> result = new ArrayList<>();
        if (elem.getCustomFacts() == null) {
            return result;
        }
        for (CustomFact fact : elem.getCustomFacts()) {
            if (fact.getTag().equals(tag) && fact.getType() != null && fact.getType().getValue() != null && fact.getType()
                    .getValue().equals(type)) {
                result.add(fact);
            }
        }
        return result;
    }

    /**
     * Get the first available description value that matches the supplied custom tag
     * 
     * @param hct
     *            the object that has custom tags
     * @param tag
     *            the tag we are looking for
     * @return the first available description value that matches the supplied custom tag
     */
    protected String getDescriptionForCustomTag(HasCustomFacts hct, String tag) {
        List<CustomFact> cfs = hct.getCustomFactsWithTag(tag);
        for (CustomFact cf : cfs) {
            if (cf != null && cf.getDescription() != null) {
                return cf.getDescription().getValue();
            }
        }
        return null;
    }

    /**
     * Is the custom fact non-null and does it have the required tag?
     * 
     * @param fact
     *            the fact
     * @param requiredTag
     *            the required tag
     * @return true if and only if the custom fact is non-null and has the specified required tag
     */
    protected boolean isNonNullAndHasRequiredTag(CustomFact fact, String requiredTag) {
        return fact != null && requiredTag.equals(fact.getTag());
    }

    /**
     * Replaces all the custom facts of a specific type with a list of other custom facts
     * 
     * @param hct
     *            the object that has custom facts to be replaced and added to. Required.
     * @param tag
     *            the tag of the facts being removed from the object. Required.
     * @param facts
     *            the facts being added to the object. Optional.
     */
    protected void replaceAllCustomFactsOfTypeWithNewFacts(HasCustomFacts hct, String tag, List<CustomFact> facts) {
        clearCustomTagsOfType(hct, tag);
        if (facts != null && !facts.isEmpty()) {
            hct.getCustomFacts(true).addAll(facts);
        }
    }

    /**
     * Set the description for a given custom tag. Used for tags that only take a description, occur no more than once for an
     * object, and have no child tags.
     * 
     * @param hcf
     *            the object that has custom facts
     * @param tag
     *            the tag to use
     * @param value
     *            the value to set on the description. Optional; pass null to remove the description custom fact.
     */
    protected void setDescriptionForCustomTag(HasCustomFacts hcf, String tag, String value) {
        clearCustomTagsOfType(hcf, tag);
        if (value != null && !value.trim().isEmpty()) {
            CustomFact cf = new CustomFact(tag);
            cf.setDescription(value);
            hcf.getCustomFacts(true).add(cf);
        }
    }
}
