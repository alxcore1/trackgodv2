package com.trackgod.app.core.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyCategoryMapperTest {

    @Test
    fun mapsLegacyV1CategoriesToStatsFriendlyCategories() {
        assertEquals("Chest", LegacyCategoryMapper.normalize("Upper Body"))
        assertEquals("Arms", LegacyCategoryMapper.normalize("Biceps"))
        assertEquals("Arms", LegacyCategoryMapper.normalize("Triceps"))
        assertEquals("Legs", LegacyCategoryMapper.normalize("Calves"))
        assertEquals("Legs", LegacyCategoryMapper.normalize("Lower Body"))
        assertEquals("Core", LegacyCategoryMapper.normalize("Abs"))
    }

    @Test
    fun identifiesCategoriesThatCanBeSafelyImprovedFromLegacySources() {
        assertTrue(LegacyCategoryMapper.canReplaceExisting("Other"))
        assertTrue(LegacyCategoryMapper.canReplaceExisting("Upper Body"))
        assertTrue(LegacyCategoryMapper.canReplaceExisting(""))
        assertFalse(LegacyCategoryMapper.canReplaceExisting("Chest"))
        assertFalse(LegacyCategoryMapper.canReplaceExisting("Back"))
    }
}
