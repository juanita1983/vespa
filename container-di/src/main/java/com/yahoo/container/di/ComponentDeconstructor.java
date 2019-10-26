// Copyright 2017 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.container.di;

/**
 * @author gjoranv
 * @author Tony Vaagenes
 */
public interface ComponentDeconstructor {
    void deconstruct(Object component);

    /**
     * Adds a task to uninstall the oldest set of obsolete bundles. This method should be called right
     * after the obsolete components of this set of bundles have been deconstructed.
     * The deconstructor only handles the timing of uninstalling, not which bundles to uninstall,
     * and hence does not take the bundles as argument.
     */
    void uninstallBundles();
}
