package com.github.romualdrousseau.archery.commons.logging;

import org.slf4j.Logger;

public interface LoggerWithLabels extends Logger {

    LoggerWithLabels setLabels(LoggerLabels labels);

    LoggerWithLabels withLabels(LoggerLabels labels);
}
