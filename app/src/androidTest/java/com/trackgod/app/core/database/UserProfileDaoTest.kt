package com.trackgod.app.core.database

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.trackgod.app.core.database.entity.UserProfileEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileDaoTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val db = Room.inMemoryDatabaseBuilder(context, TrackGodDatabase::class.java)
        .allowMainThreadQueries()
        .build()

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getProfileOnceReturnsNewestProfileWhenDuplicateRowsExist() = runTest {
        val dao = db.userProfileDao()

        dao.insert(
            UserProfileEntity(
                name = "old",
                birthday = null,
                height = 183f,
                weight = 124f,
                primaryObjective = "lose_weight",
                experienceLevel = "advanced",
                weeklyTarget = 4,
                weightUnit = "kg",
                heightUnit = "cm",
                createdAt = 1_000L,
                updatedAt = 1_000L,
            ),
        )
        dao.insert(
            UserProfileEntity(
                name = "new",
                birthday = "1987-03-01",
                height = 183f,
                weight = 139f,
                primaryObjective = "gain_muscle",
                experienceLevel = "intermediate",
                weeklyTarget = 4,
                weightUnit = "kg",
                heightUnit = "cm",
                createdAt = 2_000L,
                updatedAt = 2_000L,
            ),
        )

        val profile = dao.getProfileOnce()

        assertEquals("new", profile?.name)
        assertEquals("1987-03-01", profile?.birthday)
    }
}
