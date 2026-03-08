package terminal;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CharacterCellTests {

    // Covers: full constructor, all getters, defensive copy of styles
    @Test
    void fullConstructor_setsAllFields_andDefensiveCopiesStyles() {
        Set<Style> styles = new HashSet<>();
        styles.add(Style.BOLD);
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK, styles);
        styles.add(Style.ITALIC); // mutate original

        assertAll(
                () -> assertEquals('A',         cell.getCharacter()),
                () -> assertEquals(Color.RED,   cell.getForegroundColor()),
                () -> assertEquals(Color.BLACK, cell.getBackgroundColor()),
                () -> assertTrue(cell.getStyles().contains(Style.BOLD)),
                () -> assertFalse(cell.getStyles().contains(Style.ITALIC)) // defensive copy check
        );
    }

    // Covers: three-arg constructor (delegates to full constructor)
    @Test
    void threeArgConstructor_defaultsToEmptyStyles() {
        CharacterCell cell = new CharacterCell('B', Color.GREEN, Color.WHITE);
        assertTrue(cell.getStyles().isEmpty());
    }

    // Covers: single-arg constructor (delegates to full constructor)
    @Test
    void singleArgConstructor_defaultsToWhiteOnBlackNoStyles() {
        CharacterCell cell = new CharacterCell('A');
        assertAll(
                () -> assertEquals(Color.WHITE, cell.getForegroundColor()),
                () -> assertEquals(Color.BLACK, cell.getBackgroundColor()),
                () -> assertTrue(cell.getStyles().isEmpty())
        );
    }

    // Covers: setCharacter, setForegroundColor, setBackgroundColor
    @Test
    void setters_updateFieldsCorrectly() {
        CharacterCell cell = new CharacterCell('A', Color.RED, Color.BLACK);
        cell.setCharacter('Z');
        cell.setForegroundColor(Color.CYAN);
        cell.setBackgroundColor(Color.WHITE);
        assertAll(
                () -> assertEquals('Z',         cell.getCharacter()),
                () -> assertEquals(Color.CYAN,  cell.getForegroundColor()),
                () -> assertEquals(Color.WHITE, cell.getBackgroundColor())
        );
    }

    // Covers: addStyle, removeStyle returning true, removeStyle returning false
    @Test
    void styles_addRemove_bothBranches() {
        CharacterCell cell = new CharacterCell('A');
        cell.addStyle(Style.BOLD);
        cell.addStyle(Style.ITALIC);

        assertTrue(cell.removeStyle(Style.BOLD));             // true branch — style exists
        assertFalse(cell.removeStyle(Style.UNDERLINE));       // false branch — style absent
        assertTrue(cell.getStyles().contains(Style.ITALIC));  // others unaffected
    }

    // Covers: setStyles (including the System.out.println line)
    @Test
    void setStyles_replacesExistingStyles() {
        CharacterCell cell = new CharacterCell('A');
        cell.addStyle(Style.BOLD);
        cell.setStyles(new HashSet<>(Set.of(Style.ITALIC)));
        assertAll(
                () -> assertTrue(cell.getStyles().contains(Style.ITALIC)),
                () -> assertFalse(cell.getStyles().contains(Style.BOLD))
        );
    }

    // Covers: toString with a valid character
    @Test
    void toString_returnsCharacterAsString() {
        CharacterCell cell = new CharacterCell('X');
        assertEquals("X", cell.toString());
    }

    // Covers: toString throwing NPE when character is null
    @Test
    void toString_nullCharacter_throwsNullPointerException() {
        CharacterCell cell = new CharacterCell(null, Color.RED, Color.BLACK);
        assertThrows(NullPointerException.class, cell::toString);
    }
}