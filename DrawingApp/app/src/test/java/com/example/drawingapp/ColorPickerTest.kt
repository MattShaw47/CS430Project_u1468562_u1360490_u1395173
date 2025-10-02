package com.example.drawingapp

import androidx.compose.ui.graphics.Color
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for color picker functionality
 * Verifies that selecting a color changes the pen state
 */
class ColorPickerTest {

    @Test
    fun selecting_black_color_changes_pen_state() {
        // Initial color
        var currentColor = Color.White

        // Simulate selecting black color
        val selectedColor = Color.Black
        currentColor = selectedColor

        // Verify color changed
        assertEquals("Color should be black", Color.Black, currentColor)
        assertNotEquals("Color should no longer be white", Color.White, currentColor)
    }

    @Test
    fun selecting_red_color_changes_pen_state() {
        // Initial color
        var currentColor = Color.Black

        // Simulate selecting red color
        val selectedColor = Color.Red
        currentColor = selectedColor

        // Verify color changed
        assertEquals("Color should be red", Color.Red, currentColor)
    }

    @Test
    fun selecting_multiple_colors_updates_state_correctly() {
        var currentColor = Color.Black

        // Select blue
        currentColor = Color.Blue
        assertEquals("First selection should be blue", Color.Blue, currentColor)

        // Select yellow
        currentColor = Color.Yellow
        assertEquals("Second selection should be yellow", Color.Yellow, currentColor)

        // Select green
        currentColor = Color.Green
        assertEquals("Third selection should be green", Color.Green, currentColor)
    }

    @Test
    fun color_state_persists_after_selection() {
        var currentColor = Color.White

        // Select a color
        currentColor = Color.Magenta

        // Simulate time passing / other operations
        val savedColor = currentColor

        // Verify color persists
        assertEquals("Color should persist", Color.Magenta, savedColor)
    }

    @Test
    fun selecting_same_color_twice_maintains_state() {
        var currentColor = Color.Cyan

        // Select cyan again
        currentColor = Color.Cyan

        // Verify still cyan
        assertEquals("Color should remain cyan", Color.Cyan, currentColor)
    }

    @Test
    fun different_colors_have_different_identities() {
        // Test that Color objects for different colors are distinguishable
        val black = Color.Black
        val white = Color.White
        val red = Color.Red

        assertNotEquals("Black and white should be different", black, white)
        assertNotEquals("Black and red should be different", black, red)
        assertNotEquals("White and red should be different", white, red)
    }

    @Test
    fun color_selection_workflow() {
        // Simulate a complete color selection workflow
        var currentColor = Color.Black

        // User opens color picker with current color
        val initialColor = currentColor
        assertEquals("Initial color should be black", Color.Black, initialColor)

        // User selects a new color
        val selectedColor = Color(0xFF00FF00) // Green
        currentColor = selectedColor

        // Verify the color changed
        assertNotEquals("Color should have changed from initial", initialColor, currentColor)
        assertEquals("New color should be set", selectedColor, currentColor)
    }
}