package com.github.romualdrousseau.archery.commons.logging;

import com.github.romualdrousseau.archery.commons.logging.impl.LoggerJsonWithLabels;

public class LoggerFactory {

    public static LoggerWithLabels getLogger(Class<?> clazz) {
        return new LoggerJsonWithLabels(org.slf4j.LoggerFactory.getLogger(clazz));
    }
}
