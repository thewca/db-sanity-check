package org.worldcubeassociation.dbsanitycheck.util;

import org.worldcubeassociation.dbsanitycheck.model.SanityCheck;
import org.worldcubeassociation.dbsanitycheck.model.SanityCheckCategory;

import java.util.ArrayList;

public class StubUtil {
    private StubUtil() {
    }

    public static SanityCheck getDefaultSanityCheck(SanityCheckCategory sanityCheckCategory, int id) {
        SanityCheck sanityCheck = new SanityCheck();
        sanityCheck.setId(id);
        sanityCheck.setQuery("Query " + id);
        sanityCheck.setExclusions(new ArrayList<>());
        sanityCheck.setTopic("Topic " + id);
        sanityCheck.setSanityCheckCategoryId(sanityCheckCategory.getId());
        sanityCheck.setSanityCheckCategory(sanityCheckCategory);
        sanityCheck.setComments("Comment " + id);
        return sanityCheck;
    }

    public static SanityCheckCategory getDefaultSanityCheckCategory(int id) {
        SanityCheckCategory sanityCheckCategory = new SanityCheckCategory();
        sanityCheckCategory.setId(id);
        sanityCheckCategory.setName("Category " + id);
        return sanityCheckCategory;
    }

    public static SanityCheck getDefaultSanityCheck(int id) {
        return getDefaultSanityCheck(getDefaultSanityCheckCategory(id), id);
    }
}
