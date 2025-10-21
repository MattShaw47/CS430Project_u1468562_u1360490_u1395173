package com.example.drawingapp

import com.example.drawingapp.data.UserSettings
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Unit tests for Settings functionality
 */
class SettingsTest {

    private lateinit var userSettings: UserSettings

    @Before
    fun setup() {
        userSettings = UserSettings()
    }

    @Test
    fun default_settings_are_correct() {
        assertEquals("Cloud backup should be disabled by default", false, userSettings.cloudBackupEnabled)
        assertEquals("Sync should be disabled by default", false, userSettings.syncEnabled)
        assertEquals("Default account name should be Guest User", "Guest User", userSettings.userAccountName)
    }

    @Test
    fun enabling_cloud_backup_changes_state() {
        val updatedSettings = userSettings.copy(cloudBackupEnabled = true)

        assertTrue("Cloud backup should be enabled", updatedSettings.cloudBackupEnabled)
        assertNotEquals("Settings should have changed", userSettings.cloudBackupEnabled, updatedSettings.cloudBackupEnabled)
    }

    @Test
    fun disabling_cloud_backup_changes_state() {
        val enabledSettings = userSettings.copy(cloudBackupEnabled = true)
        val disabledSettings = enabledSettings.copy(cloudBackupEnabled = false)

        assertFalse("Cloud backup should be disabled", disabledSettings.cloudBackupEnabled)
    }

    @Test
    fun enabling_sync_changes_state() {
        val updatedSettings = userSettings.copy(syncEnabled = true)

        assertTrue("Sync should be enabled", updatedSettings.syncEnabled)
        assertNotEquals("Settings should have changed", userSettings.syncEnabled, updatedSettings.syncEnabled)
    }

    @Test
    fun disabling_sync_changes_state() {
        val enabledSettings = userSettings.copy(syncEnabled = true)
        val disabledSettings = enabledSettings.copy(syncEnabled = false)

        assertFalse("Sync should be disabled", disabledSettings.syncEnabled)
    }

    @Test
    fun changing_account_name_updates_correctly() {
        val newName = "John Doe"
        val updatedSettings = userSettings.copy(userAccountName = newName)

        assertEquals("Account name should be updated", newName, updatedSettings.userAccountName)
        assertNotEquals("Account name should have changed", userSettings.userAccountName, updatedSettings.userAccountName)
    }

    @Test
    fun account_name_can_be_empty() {
        val updatedSettings = userSettings.copy(userAccountName = "")

        assertEquals("Account name should be empty", "", updatedSettings.userAccountName)
        assertTrue("Account name should be empty string", updatedSettings.userAccountName.isEmpty())
    }

    @Test
    fun multiple_settings_can_be_changed_together() {
        val updatedSettings = userSettings.copy(
            cloudBackupEnabled = true,
            syncEnabled = true,
            userAccountName = "Test User"
        )

        assertTrue("Cloud backup should be enabled", updatedSettings.cloudBackupEnabled)
        assertTrue("Sync should be enabled", updatedSettings.syncEnabled)
        assertEquals("Account name should be updated", "Test User", updatedSettings.userAccountName)
    }

    @Test
    fun settings_immutability_with_data_class() {
        val settings1 = UserSettings(
            cloudBackupEnabled = true,
            syncEnabled = false,
            userAccountName = "User1"
        )

        val settings2 = settings1.copy(cloudBackupEnabled = false)

        // Original should be unchanged
        assertTrue("Original cloudBackup should still be true", settings1.cloudBackupEnabled)
        assertFalse("New settings cloudBackup should be false", settings2.cloudBackupEnabled)

        // Other properties should remain the same
        assertEquals("Sync should be same in both", settings1.syncEnabled, settings2.syncEnabled)
        assertEquals("Account name should be same in both", settings1.userAccountName, settings2.userAccountName)
    }

    @Test
    fun settings_equality_works_correctly() {
        val settings1 = UserSettings(
            cloudBackupEnabled = true,
            syncEnabled = true,
            userAccountName = "Test"
        )

        val settings2 = UserSettings(
            cloudBackupEnabled = true,
            syncEnabled = true,
            userAccountName = "Test"
        )

        assertEquals("Identical settings should be equal", settings1, settings2)
    }

    @Test
    fun settings_inequality_works_correctly() {
        val settings1 = UserSettings(
            cloudBackupEnabled = true,
            syncEnabled = true,
            userAccountName = "Test"
        )

        val settings2 = UserSettings(
            cloudBackupEnabled = false,
            syncEnabled = true,
            userAccountName = "Test"
        )

        assertNotEquals("Different settings should not be equal", settings1, settings2)
    }

    @Test
    fun toggling_settings_multiple_times() {
        var currentSettings = userSettings

        // Toggle cloud backup multiple times
        currentSettings = currentSettings.copy(cloudBackupEnabled = true)
        assertTrue("Should be enabled after first toggle", currentSettings.cloudBackupEnabled)

        currentSettings = currentSettings.copy(cloudBackupEnabled = false)
        assertFalse("Should be disabled after second toggle", currentSettings.cloudBackupEnabled)

        currentSettings = currentSettings.copy(cloudBackupEnabled = true)
        assertTrue("Should be enabled after third toggle", currentSettings.cloudBackupEnabled)
    }

    @Test
    fun cloud_backup_and_sync_are_independent() {
        val settings = UserSettings(
            cloudBackupEnabled = true,
            syncEnabled = false,
            userAccountName = "Test"
        )

        assertTrue("Cloud backup can be enabled", settings.cloudBackupEnabled)
        assertFalse("While sync is disabled", settings.syncEnabled)

        // They should be independently toggleable
        val newSettings = settings.copy(syncEnabled = true)
        assertTrue("Both can be enabled", newSettings.cloudBackupEnabled && newSettings.syncEnabled)
    }

    @Test
    fun account_name_accepts_various_formats() {
        val names = listOf(
            "John Doe",
            "john.doe@example.com",
            "User123",
            "用户", // Chinese
            "A".repeat(100) // Long name
        )

        names.forEach { name ->
            val settings = userSettings.copy(userAccountName = name)
            assertEquals("Should accept name: $name", name, settings.userAccountName)
        }
    }

    @Test
    fun settings_workflow_simulation() {
        // User opens settings
        var currentSettings = UserSettings()
        assertFalse("Initial backup disabled", currentSettings.cloudBackupEnabled)

        // User enables cloud backup
        currentSettings = currentSettings.copy(cloudBackupEnabled = true)
        assertTrue("Backup now enabled", currentSettings.cloudBackupEnabled)

        // User changes account name
        currentSettings = currentSettings.copy(userAccountName = "My Account")
        assertEquals("Account name updated", "My Account", currentSettings.userAccountName)

        // User enables sync
        currentSettings = currentSettings.copy(syncEnabled = true)
        assertTrue("Sync now enabled", currentSettings.syncEnabled)

        // Verify all changes persisted
        assertTrue("Cloud backup still enabled", currentSettings.cloudBackupEnabled)
        assertTrue("Sync still enabled", currentSettings.syncEnabled)
        assertEquals("Account name still correct", "My Account", currentSettings.userAccountName)
    }
}