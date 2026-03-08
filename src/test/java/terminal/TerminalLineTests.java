package terminal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TerminalLineTests {

    private TerminalLine line;
    private static final int WIDTH = 10;

    @BeforeEach
    void setUp() {
        line = new TerminalLine(WIDTH);
    }

    // --- Constructor ---

    @Test
    void constructor_initializesCellsWithCorrectWidth() {
        assertNotNull(line.getCells());
        assertEquals(WIDTH, line.getCells().length);
    }

    @Test
    void constructor_initializesAllCellsToNull() {
        for (CharacterCell cell : line.getCells()) {
            assertNull(cell);
        }
    }

    @Test
    void constructor_initializesWrappedToFalse() {
        assertFalse(line.isWrapped());
    }

    @Test
    void constructor_withWidthOne_createsSingleCellArray() {
        TerminalLine singleLine = new TerminalLine(1);
        assertEquals(1, singleLine.getCells().length);
    }

    // --- setCell / getCell ---

    @Test
    void setCell_storesCell_andGetCellReturnsIt() {
        CharacterCell mockCell = mock(CharacterCell.class);
        line.setCell(0, mockCell);
        assertSame(mockCell, line.getCell(0));
    }

    @Test
    void setCell_atLastIndex_storesCorrectly() {
        CharacterCell mockCell = mock(CharacterCell.class);
        line.setCell(WIDTH - 1, mockCell);
        assertSame(mockCell, line.getCell(WIDTH - 1));
    }

    @Test
    void setCell_withNull_storesNull() {
        CharacterCell mockCell = mock(CharacterCell.class);
        line.setCell(3, mockCell);
        line.setCell(3, null);
        assertNull(line.getCell(3));
    }

    @Test
    void setCell_overwritesPreviousCell() {
        CharacterCell firstCell = mock(CharacterCell.class);
        CharacterCell secondCell = mock(CharacterCell.class);
        line.setCell(2, firstCell);
        line.setCell(2, secondCell);
        assertSame(secondCell, line.getCell(2));
    }

    @Test
    void getCell_onUninitializedIndex_returnsNull() {
        assertNull(line.getCell(5));
    }

    @Test
    void setCell_outOfBounds_throwsArrayIndexOutOfBoundsException() {
        CharacterCell mockCell = mock(CharacterCell.class);
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> line.setCell(WIDTH, mockCell));
    }

    @Test
    void setCell_negativeIndex_throwsArrayIndexOutOfBoundsException() {
        CharacterCell mockCell = mock(CharacterCell.class);
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> line.setCell(-1, mockCell));
    }

    // --- getCells ---

    @Test
    void getCells_returnsUnderlyingArray() {
        CharacterCell mockCell = mock(CharacterCell.class);
        line.setCell(4, mockCell);
        CharacterCell[] cells = line.getCells();
        assertSame(mockCell, cells[4]);
    }

    @Test
    void getCells_returnsArrayOfCorrectLength() {
        assertEquals(WIDTH, line.getCells().length);
    }

    // --- isWrapped / setWrapped ---

    @Test
    void setWrapped_toTrue_isWrappedReturnsTrue() {
        line.setWrapped(true);
        assertTrue(line.isWrapped());
    }

    @Test
    void setWrapped_toFalse_isWrappedReturnsFalse() {
        line.setWrapped(true);
        line.setWrapped(false);
        assertFalse(line.isWrapped());
    }

    @Test
    void setWrapped_toggleMultipleTimes_reflectsLastValue() {
        line.setWrapped(true);
        line.setWrapped(false);
        line.setWrapped(true);
        assertTrue(line.isWrapped());
    }

    // --- toString ---

    @Test
    void toString_allNullCells_returnsSpaces() {
        String result = line.toString();
        assertEquals(" ".repeat(WIDTH), result);
    }

    @Test
    void toString_withPopulatedCells_appendsCellToString() {
        CharacterCell mockCell = mock(CharacterCell.class);
        when(mockCell.toString()).thenReturn("A");
        line.setCell(0, mockCell);

        String result = line.toString();
        // First char is "A", remaining WIDTH-1 chars are spaces
        assertEquals("A" + " ".repeat(WIDTH - 1), result);
    }

    @Test
    void toString_withMixedCells_combinesCorrectly() {
        CharacterCell cellA = mock(CharacterCell.class);
        CharacterCell cellB = mock(CharacterCell.class);
        when(cellA.toString()).thenReturn("X");
        when(cellB.toString()).thenReturn("Y");

        line.setCell(0, cellA);
        line.setCell(WIDTH - 1, cellB);

        String result = line.toString();
        assertEquals("X" + " ".repeat(WIDTH - 2) + "Y", result);
    }

    @Test
    void toString_lengthMatchesWidth() {
        assertEquals(WIDTH, line.toString().length());
    }

    @Test
    void toString_withAllCellsSet_noSpaces() {
        for (int i = 0; i < WIDTH; i++) {
            CharacterCell mockCell = mock(CharacterCell.class);
            when(mockCell.toString()).thenReturn("Z");
            line.setCell(i, mockCell);
        }
        assertEquals("Z".repeat(WIDTH), line.toString());
    }
}