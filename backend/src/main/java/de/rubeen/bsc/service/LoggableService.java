package de.rubeen.bsc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LoggableService {
    protected Logger LOG = LoggerFactory.getLogger(this.getClass());
}
