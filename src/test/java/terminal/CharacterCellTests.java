package terminal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterCellTests {

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Full constructor sets all fields correctly")
    void fullConstructor_setsAllFields() {
        Set<Style> styles = Set.of(Style.BOLD, Style.ITALIC);
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK, styles);

        assertAll(
                () -> assertEquals('A',         cell.getCharacter()),
                () -> assertEquals(Color.RED,   cell.getForegroundColor()),
                () -> assertEquals(Color.BLACK, cell.getBackgroundColor()),
                () -> assertTrue(cell.getStyles().contains(Style.BOLD)),
                () -> assertTrue(cell.getStyles().contains(Style.ITALIC))
        );
    }

    @Test
    @DisplayName("Convenience constructor defaults to empty styles")
    void convenienceConstructor_stylesIsEmpty() {
        CharacterCell cell = new CharacterCell('B', Color.GREEN, Color.WHITE);

        assertAll(
                () -> assertEquals('B',          cell.getCharacter()),
                () -> assertEquals(Color.GREEN,  cell.getForegroundColor()),
                () -> assertEquals(Color.WHITE,  cell.getBackgroundColor()),
                () -> assertTrue(cell.getStyles().isEmpty())
        );
    }

    @Test
    @DisplayName("Full constructor defensive-copies the styles set")
    void fullConstructor_defensiveCopiesStyles() {
        Set<Style> styles = new HashSet<>();
        styles.add(Style.BOLD);
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK, styles);

        styles.add(Style.ITALIC); // mutate original

        assertFalse(cell.getStyles().contains(Style.ITALIC));
    }

    @Test
    @DisplayName("Full constructor accepts null character")
    void fullConstructor_nullCharacter() {
        CharacterCell cell = new CharacterCell(null, Color.RED, Color.BLACK, new HashSet<>());
        assertNull(cell.getCharacter());
    }

    @Test
    @DisplayName("Full constructor accepts null foreground color")
    void fullConstructor_nullForegroundColor() {
        CharacterCell cell = new CharacterCell('A', null, Color.BLACK, new HashSet<>());
        assertNull(cell.getForegroundColor());
    }

    @Test
    @DisplayName("Full constructor accepts null background color")
    void fullConstructor_nullBackgroundColor() {
        CharacterCell cell = new CharacterCell('A', Color.RED, null, new HashSet<>());
        assertNull(cell.getBackgroundColor());
    }

    // -------------------------------------------------------------------------
    // Character
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("setCharacter stores and getCharacter retrieves the value")
    void setCharacter_roundtrip() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.setCharacter('Z');
        assertEquals('Z', cell.getCharacter());
    }

    @Test
    @DisplayName("setCharacter overwrites a previous value")
    void setCharacter_overwritesPreviousValue() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.setCharacter('B');
        assertEquals('B', cell.getCharacter());
    }

    @Test
    @DisplayName("setCharacter(null) marks the cell as empty")
    void setCharacter_null_cellIsEmpty() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.setCharacter(null);
        assertNull(cell.getCharacter());
    }

    @Test
    @DisplayName("setCharacter accepts a wide range of characters")
    void setCharacter_variousCharacters() {
        CharacterCell cell = new CharacterCell(null, null, null);
        char[] chars = {'a', 'Z', '0', '9', ' ', '!', '@', '\u00e9', '\u4e2d'};
        for (char c : chars) {
            cell.setCharacter(c);
            assertEquals(c, cell.getCharacter());
        }
    }

    // -------------------------------------------------------------------------
    // Foreground color
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("setForegroundColor stores and getForegroundColor retrieves the value")
    void setForegroundColor_roundtrip() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.setForegroundColor(Color.CYAN);
        assertEquals(Color.CYAN, cell.getForegroundColor());
    }

    @ParameterizedTest
    @EnumSource(Color.class)
    @DisplayName("setForegroundColor accepts every Color enum constant")
    void setForegroundColor_allColors(Color color) {
        CharacterCell cell = new CharacterCell('A', null, Color.BLACK);
        cell.setForegroundColor(color);
        assertEquals(color, cell.getForegroundColor());
    }

    @Test
    @DisplayName("setForegroundColor(null) resets the foreground color")
    void setForegroundColor_null_resetsColor() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.setForegroundColor(null);
        assertNull(cell.getForegroundColor());
    }

    @Test
    @DisplayName("setForegroundColor overwrites a previous value")
    void setForegroundColor_overwritesPreviousValue() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.setForegroundColor(Color.MAGENTA);
        assertEquals(Color.MAGENTA, cell.getForegroundColor());
    }

    // -------------------------------------------------------------------------
    // Background color
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("setBackgroundColor stores and getBackgroundColor retrieves the value")
    void setBackgroundColor_roundtrip() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.setBackgroundColor(Color.CYAN);
        assertEquals(Color.CYAN, cell.getBackgroundColor());
    }

    @ParameterizedTest
    @EnumSource(Color.class)
    @DisplayName("setBackgroundColor accepts every Color enum constant")
    void setBackgroundColor_allColors(Color color) {
        CharacterCell cell = new CharacterCell('A', Color.RED, null);
        cell.setBackgroundColor(color);
        assertEquals(color, cell.getBackgroundColor());
    }

    @Test
    @DisplayName("setBackgroundColor(null) resets the background color")
    void setBackgroundColor_null_resetsColor() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.setBackgroundColor(null);
        assertNull(cell.getBackgroundColor());
    }

    @Test
    @DisplayName("setBackgroundColor overwrites a previous value")
    void setBackgroundColor_overwritesPreviousValue() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.setBackgroundColor(Color.YELLOW);
        assertEquals(Color.YELLOW, cell.getBackgroundColor());
    }

    // -------------------------------------------------------------------------
    // Styles — addStyle
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("addStyle adds a single style")
    void addStyle_singleStyle() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.addStyle(Style.BOLD);
        assertTrue(cell.getStyles().contains(Style.BOLD));
    }

    @ParameterizedTest
    @EnumSource(Style.class)
    @DisplayName("addStyle accepts every Style enum constant")
    void addStyle_allStyles(Style style) {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.addStyle(style);
        assertTrue(cell.getStyles().contains(style));
    }

    @Test
    @DisplayName("addStyle can hold all styles simultaneously")
    void addStyle_allStylesAtOnce() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.addStyle(Style.BOLD);
        cell.addStyle(Style.ITALIC);
        cell.addStyle(Style.UNDERLINE);

        assertAll(
                () -> assertTrue(cell.getStyles().contains(Style.BOLD)),
                () -> assertTrue(cell.getStyles().contains(Style.ITALIC)),
                () -> assertTrue(cell.getStyles().contains(Style.UNDERLINE)),
                () -> assertEquals(3, cell.getStyles().size())
        );
    }

    @Test
    @DisplayName("addStyle is idempotent — adding the same style twice doesn't duplicate it")
    void addStyle_duplicate_noEffect() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.addStyle(Style.BOLD);
        cell.addStyle(Style.BOLD);
        assertEquals(1, cell.getStyles().size());
    }

    @Test
    @DisplayName("Styles passed to constructor are present via getStyles")
    void constructor_stylesAvailableViaGetter() {
        Set<Style> styles = Set.of(Style.BOLD, Style.UNDERLINE);
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK, styles);

        assertAll(
                () -> assertTrue(cell.getStyles().contains(Style.BOLD)),
                () -> assertTrue(cell.getStyles().contains(Style.UNDERLINE)),
                () -> assertEquals(2, cell.getStyles().size())
        );
    }

    // -------------------------------------------------------------------------
    // Styles — removeStyle
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("removeStyle removes an existing style and returns true")
    void removeStyle_existingStyle_returnsTrue() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.addStyle(Style.ITALIC);

        assertTrue(cell.removeStyle(Style.ITALIC));
        assertFalse(cell.getStyles().contains(Style.ITALIC));
    }

    @Test
    @DisplayName("removeStyle on absent style returns false and leaves styles unchanged")
    void removeStyle_absentStyle_returnsFalse() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.addStyle(Style.BOLD);

        assertFalse(cell.removeStyle(Style.UNDERLINE));
        assertEquals(1, cell.getStyles().size());
        assertTrue(cell.getStyles().contains(Style.BOLD));
    }

    @Test
    @DisplayName("removeStyle only removes the targeted style, leaving others intact")
    void removeStyle_doesNotAffectOtherStyles() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.addStyle(Style.BOLD);
        cell.addStyle(Style.ITALIC);
        cell.removeStyle(Style.BOLD);

        assertAll(
                () -> assertFalse(cell.getStyles().contains(Style.BOLD)),
                () -> assertTrue(cell.getStyles().contains(Style.ITALIC))
        );
    }

    @Test
    @DisplayName("removeStyle on empty styles set returns false")
    void removeStyle_emptySet_returnsFalse() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        assertFalse(cell.removeStyle(Style.BOLD));
    }

    // -------------------------------------------------------------------------
    // Independence
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Setting one attribute does not affect the others")
    void setAttribute_doesNotAffectOthers() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.WHITE);
        cell.addStyle(Style.UNDERLINE);

        cell.setCharacter('B');

        assertAll(
                () -> assertEquals('B',          cell.getCharacter()),
                () -> assertEquals(Color.RED,    cell.getForegroundColor()),
                () -> assertEquals(Color.WHITE,  cell.getBackgroundColor()),
                () -> assertTrue(cell.getStyles().contains(Style.UNDERLINE))
        );
    }

    @Test
    @DisplayName("Two CharacterCell instances are independent")
    void twoInstances_areIndependent() {
        CharacterCell a = new CharacterCell('X', Color.MAGENTA, Color.BLACK);
        CharacterCell b = new CharacterCell('Y', Color.YELLOW, Color.WHITE);

        a.addStyle(Style.BOLD);
        b.addStyle(Style.ITALIC);

        assertAll(
                () -> assertEquals('X',           a.getCharacter()),
                () -> assertEquals(Color.MAGENTA, a.getForegroundColor()),
                () -> assertTrue(a.getStyles().contains(Style.BOLD)),
                () -> assertFalse(a.getStyles().contains(Style.ITALIC)),
                () -> assertEquals('Y',           b.getCharacter()),
                () -> assertEquals(Color.YELLOW,  b.getForegroundColor()),
                () -> assertTrue(b.getStyles().contains(Style.ITALIC)),
                () -> assertFalse(b.getStyles().contains(Style.BOLD))
        );
    }
}