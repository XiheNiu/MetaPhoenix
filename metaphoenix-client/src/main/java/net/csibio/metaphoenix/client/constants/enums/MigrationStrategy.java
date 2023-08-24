package net.csibio.metaphoenix.client.constants.enums;

public enum MigrationStrategy {

    None("None"),
    Override("Override"),
    Combine("Combine"),
    ;

    private final String name;

    MigrationStrategy(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static MigrationStrategy getByName(String name, MigrationStrategy defaultStrategy) {
        for (MigrationStrategy strategy : values()) {
            if (strategy.getName().equals(name)) {
                return strategy;
            }
        }
        return defaultStrategy;
    }
}
