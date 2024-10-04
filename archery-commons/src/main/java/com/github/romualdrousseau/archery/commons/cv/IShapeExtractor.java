package com.github.romualdrousseau.archery.commons.cv;

import java.util.List;

public abstract class IShapeExtractor {

    public abstract List<SearchPoint[]> extractAll(ISearchBitmap bitmap);

    public abstract SearchPoint[] extractBest(ISearchBitmap bitmap);
}
