package io.bootique.tools.release.model.maven;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DependencyTest {

    private Dependency dependency;

    @BeforeEach
    public void createDependency() {
        dependency = new Dependency("group1:id1:jar:1.0:test");
    }

    @Test
    void getName() {
        assertEquals("group1:id1:jar:1.0:test", dependency.getName());
    }

    @Test
    void getModule() {
        Module module = dependency.getModule();
        assertNotNull(module);
        assertEquals("group1", module.getGroup());
        assertEquals("id1", module.getId());
        assertEquals("1.0", module.getVersion());
    }

    @Test
    void getArtifact() {
        assertEquals("jar", dependency.getArtifact());
    }

    @Test
    void getType() {
        assertEquals("test", dependency.getType());
    }

    @Test
    void compareTo() {
        Dependency dep2 = new Dependency("group1:id1:jar:1.0:compile");
        Dependency dep3 = new Dependency("group1:id1:jar:2.0:test");
        Dependency dep4 = new Dependency("group1:id2:jar:1.0:test");
        Dependency dep5 = new Dependency("group1:id0:jar:1.0:test");

        assertEquals(0, dependency.compareTo(dep2));
        assertEquals(0, dependency.compareTo(dep3));
        assertEquals(-1, dependency.compareTo(dep4));
        assertEquals(1, dependency.compareTo(dep5));
    }

    @Test
    void equals() {
        Dependency dep2 = new Dependency("group1:id1:jar:1.0:compile");
        Dependency dep3 = new Dependency("group1:id1:jar:2.0:test");
        Dependency dep4 = new Dependency("group1:id2:jar:1.0:test");

        assertEquals(dep2, dependency);
        assertEquals(dep3, dependency);
        assertNotEquals(dep4, dependency);
    }

    @Test
    void testHashCode() {
        Dependency dep2 = new Dependency("group1:id1:jar:1.0:compile");
        Dependency dep3 = new Dependency("group1:id1:jar:2.0:test");
        Dependency dep4 = new Dependency("group1:id2:jar:1.0:test");

        assertEquals(dep2.hashCode(), dependency.hashCode());
        assertEquals(dep3.hashCode(), dependency.hashCode());
        assertNotEquals(dep4.hashCode(), dependency.hashCode());
    }

}