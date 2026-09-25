package com.lifevault.app.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Scaffold for Section 5.6's migration testing requirement ("every schema change ships a
 * Migration + an instrumented MigrationTestHelper test"). There is only version 1 today,
 * so this just proves the exported schema opens cleanly; it becomes the pattern later
 * migrations extend — e.g. `helper.runMigrationsAndValidate(DATABASE_NAME, 2, true, MIGRATION_1_2)`.
 *
 * Not executed in this build environment (no device/emulator; see BUILD_LOG.md Step 5).
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        LifeVaultDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun version1SchemaCreatesCleanly() {
        helper.createDatabase(DATABASE_NAME, 1).close()
    }
}
