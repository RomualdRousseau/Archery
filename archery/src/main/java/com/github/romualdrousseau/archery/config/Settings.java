package com.github.romualdrousseau.archery.config;

public class Settings {
    public final static int DEFAULT_SAMPLE_COUNT = 200;
    public final static int MAX_STORE_ROWS = 10000;

    public final static float DEFAULT_RATIO_SIMILARITY = 0.35f;
    public final static float DEFAULT_ENTITY_PROBABILITY = 0.45f;
    public final static float DEFAULT_CAPILLARITY_THRESHOLD = 0.5f;

    public final static String PIVOT_KEY_SUFFIX = "#PIVOT?";
    public final static String PIVOT_VALUE_SUFFIX = "#VALUE?";
    public static final String PIVOT_TYPE_SUFFIX = "#TYPE?";
    public final static String GROUP_VALUE_SUFFIX = "#GROUP?";
    public final static String COLUMN_VALUE_SUFFIX = "#COLUMN?";

    public static final String MERGE_SEPARATOR = " ";
}
