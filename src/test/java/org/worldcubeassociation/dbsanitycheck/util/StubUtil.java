package org.worldcubeassociation.dbsanitycheck.util;

import org.worldcubeassociation.dbsanitycheck.model.SanityCheck;
import org.worldcubeassociation.dbsanitycheck.model.Category;

import java.util.ArrayList;

public class StubUtil {
    private StubUtil() {
    }

    public static SanityCheck getDefaultSanityCheck(Category category, int id) {
        SanityCheck sanityCheck = new SanityCheck();
        sanityCheck.setId(id);
        sanityCheck.setQuery("Query " + id);
        sanityCheck.setExclusions(new ArrayList<>());
        sanityCheck.setTopic("Topic " + id);
        sanityCheck.setSanityCheckCategoryId(category.getId());
        sanityCheck.setCategory(category);
        sanityCheck.setComments("Comment " + id);
        return sanityCheck;
    }

    public static Category getDefaultSanityCheckCategory(int id) {
        Category category = new Category();
        category.setId(id);
        category.setName("Category " + id);
        return category;
    }

    public static SanityCheck getDefaultSanityCheck(int id) {
        return getDefaultSanityCheck(getDefaultSanityCheckCategory(id), id);
    }
}
