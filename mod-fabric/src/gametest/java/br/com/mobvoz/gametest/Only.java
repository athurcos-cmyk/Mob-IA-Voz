package br.com.mobvoz.gametest;

/** {@code gradlew runClientGameTest -Ponly=MemoryPersistenceGameTest} roda so esse teste (padrao: todos). */
final class Only {
    private Only() {
    }

    static boolean skip(Class<?> test) {
        String only = System.getProperty("mobvoz.test.only");
        return only != null && !only.isEmpty() && !test.getSimpleName().equals(only);
    }
}
