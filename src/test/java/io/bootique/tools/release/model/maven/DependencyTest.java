package io.bootique.tools.release.model.maven;

import io.bootique.tools.release.model.maven.persistent.Dependency;
import io.bootique.tools.release.model.maven.persistent.Module;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DependencyTest {

    private Dependency dependency;

    @BeforeEach
    public void createDependency() {
        dependency = new Dependency("group1", "id1", "1.0", "test", null);
    }

    @Test
    void getModule() {
        Module module = dependency.getModule();
        assertNotNull(module);
        assertEquals("group1", module.getGroupStr());
        assertEquals("id1", module.getId());
        assertEquals("1.0", module.getVersion());
    }

    @Test
    void getType() {
        assertEquals("test", dependency.getType());
    }

    @Test
    void compareTo() {
        Dependency dep2 = new Dependency("group1", "id1", "1.0", "compile", null);
        Dependency dep3 = new Dependency("group1", "id1","2.0", "test", null);
        Dependency dep4 = new Dependency("group1", "id2", "1.0", "test", null);
        Dependency dep5 = new Dependency("group1", "id0", "1.0", "test", null);

        assertEquals(0, dependency.compareTo(dep2));
        assertEquals(0, dependency.compareTo(dep3));
        assertEquals(-1, dependency.compareTo(dep4));
        assertEquals(1, dependency.compareTo(dep5));
    }

    @Test
    void equals() {
        Dependency dep2 = new Dependency("group1", "id1", "1.0", "compile", null);
        Dependency dep3 = new Dependency("group1", "id1", "2.0", "test", null);
        Dependency dep4 = new Dependency("group1", "id2", "1.0", "test", null);

        assertEquals(dep2, dependency);
        assertEquals(dep3, dependency);
        assertNotEquals(dep4, dependency);
    }

    @Test
    void testHashCode() {
        Dependency dep2 = new Dependency("group1", "id1", "1.0", "compile", null);
        Dependency dep3 = new Dependency("group1", "id1", "2.0", "test", null);
        Dependency dep4 = new Dependency("group1", "id2", "1.0", "test", null);

        assertEquals(dep2.hashCode(), dependency.hashCode());
        assertEquals(dep3.hashCode(), dependency.hashCode());
        assertNotEquals(dep4.hashCode(), dependency.hashCode());
    }

}