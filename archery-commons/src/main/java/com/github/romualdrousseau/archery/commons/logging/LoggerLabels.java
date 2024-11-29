package com.github.romualdrousseau.archery.commons.logging;

import static net.logstash.logback.argument.StructuredArguments.kv;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.logstash.logback.argument.StructuredArgument;

public class LoggerLabels {

    @JsonIgnore
    public StructuredArgument get() {
        return kv("logging.googleapis.com/labels", this);
    }
}
