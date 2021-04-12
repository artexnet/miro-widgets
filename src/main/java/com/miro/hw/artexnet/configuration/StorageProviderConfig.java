package com.miro.hw.artexnet.configuration;

import com.miro.hw.artexnet.storage.WidgetStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageProviderConfig {

    @Value("${storage.type}")
    private String configuredStorage;

    @Value("${storage.enableDefault:#{true}}")
    private boolean enableDefault;

    // Storage implementations
    private final WidgetStorage[] storageTypes;

    /**
     * Initializes a new instance of the class.
     */
    @Autowired
    public StorageProviderConfig(WidgetStorage[] storageTypes) {
        this.storageTypes = storageTypes;
    }

    /**
     * Gets the configured/selected storage provided.
     */
    public WidgetStorage getConfiguredStorage() {
        for (WidgetStorage storage : this.storageTypes) {
            if (storage.getStorageType().name().toLowerCase().equals(configuredStorage)) {
                return storage;
            }
        }

        if (this.storageTypes.length == 1)
            return this.storageTypes[0];
        if (!enableDefault || this.storageTypes.length == 0)
            throw new RuntimeException("Storage implementation not found (configuration failure");
        return this.storageTypes[0];
    }

}
