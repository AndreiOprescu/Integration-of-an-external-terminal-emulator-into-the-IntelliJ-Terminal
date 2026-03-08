package terminal.cursor;

import org.junit.jupiter.api.Test;
import terminal.cursor.CursorPosition;

import static org.junit.jupiter.api.Assertions.*;

public class CursorPositionTests {

    // Covers: full constructor (normal), getRow, getCol, clamp (value in range)
    @Test
    void fullConstructor_validPosition_storesRowAndCol() {
        CursorPosition pos = new CursorPosition(2, 3, 10, 20);
        assertAll(
                () -> assertEquals(2, pos.getRow()),
                () -> assertEquals(3, pos.getCol())
        );
    }

    // Covers: full constructor clamp — row/col below 0 clamp to 0
    @Test
    void fullConstructor_negativeRowCol_clampsToZero() {
        CursorPosition pos = new CursorPosition(-5, -5, 10, 20);
        assertAll(
                () -> assertEquals(0, pos.getRow()),
                () -> assertEquals(0, pos.getCol())
        );
    }

    // Covers: full constructor clamp — row/col above max clamp to max-1
    @Test
    void fullConstructor_excessiveRowCol_clampsToMax() {
        CursorPosition pos = new CursorPosition(99, 99, 10, 20);
        assertAll(
                () -> assertEquals(9,  pos.getRow()),
                () -> assertEquals(19, pos.getCol())
        );
    }

    // Covers: legacy constructor (delegates to full constructor with Integer.MAX_VALUE bounds)
    @Test
    void legacyConstructor_storesRowAndCol_noBoundsEnforcement() {
        CursorPosition pos = new CursorPosition(5, 7);
        assertAll(
                () -> assertEquals(5, pos.getRow()),
                () -> assertEquals(7, pos.getCol())
        );
    }

    // Covers: setRow, setCol (normal), clamp mid-range
    @Test
    void setRowAndCol_validValues_updatesCorrectly() {
        CursorPosition pos = new CursorPosition(0, 0, 10, 20);
        pos.setRow(5);
        pos.setCol(10);
        assertAll(
                () -> assertEquals(5,  pos.getRow()),
                () -> assertEquals(10, pos.getCol())
        );
    }

    // Covers: setRow clamp high, setCol clamp high
    @Test
    void setRowAndCol_aboveMax_clampsToMax() {
        CursorPosition pos = new CursorPosition(0, 0, 10, 20);
        pos.setRow(100);
        pos.setCol(100);
        assertAll(
                () -> assertEquals(9,  pos.getRow()),
                () -> assertEquals(19, pos.getCol())
        );
    }

    // Covers: setRow clamp low, setCol clamp low
    @Test
    void setRowAndCol_belowZero_clampsToZero() {
        CursorPosition pos = new CursorPosition(5, 5, 10, 20);
        pos.setRow(-1);
        pos.setCol(-1);
        assertAll(
                () -> assertEquals(0, pos.getRow()),
                () -> assertEquals(0, pos.getCol())
        );
    }

    // Covers: setPosition
    @Test
    void setPosition_updatesRowAndCol() {
        CursorPosition pos = new CursorPosition(0, 0, 10, 20);
        pos.setPosition(3, 8);
        assertAll(
                () -> assertEquals(3, pos.getRow()),
                () -> assertEquals(8, pos.getCol())
        );
    }

    // Covers: moveLeft(), moveRight(), moveUp(), moveDown() — single step, normal
    @Test
    void singleStepMoves_normalPosition_movesCorrectly() {
        CursorPosition pos = new CursorPosition(5, 5, 10, 20);
        pos.moveRight(); assertEquals(6,  pos.getCol());
        pos.moveLeft();  assertEquals(5,  pos.getCol());
        pos.moveDown();  assertEquals(6,  pos.getRow());
        pos.moveUp();    assertEquals(5,  pos.getRow());
    }

    // Covers: moveLeft(), moveUp() clamping at 0 boundary
    @Test
    void singleStepMoves_atMinBoundary_clampsToZero() {
        CursorPosition pos = new CursorPosition(0, 0, 10, 20);
        pos.moveLeft(); assertEquals(0, pos.getCol());
        pos.moveUp();   assertEquals(0, pos.getRow());
    }

    // Covers: moveRight(), moveDown() clamping at max boundary
    @Test
    void singleStepMoves_atMaxBoundary_clampsToMax() {
        CursorPosition pos = new CursorPosition(9, 19, 10, 20);
        pos.moveRight(); assertEquals(19, pos.getCol());
        pos.moveDown();  assertEquals(9,  pos.getRow());
    }

    // Covers: moveLeft(n), moveRight(n), moveUp(n), moveDown(n) — normal multi-step
    @Test
    void multiStepMoves_normalPosition_movesCorrectly() {
        CursorPosition pos = new CursorPosition(5, 10, 20, 40);
        pos.moveRight(4); assertEquals(14, pos.getCol());
        pos.moveLeft(4);  assertEquals(10, pos.getCol());
        pos.moveDown(3);  assertEquals(8,  pos.getRow());
        pos.moveUp(3);    assertEquals(5,  pos.getRow());
    }

    // Covers: moveLeft(n), moveUp(n) clamping at 0; moveRight(n), moveDown(n) clamping at max
    @Test
    void multiStepMoves_exceedBounds_clamps() {
        CursorPosition pos = new CursorPosition(5, 5, 10, 20);
        pos.moveLeft(100);  assertEquals(0,  pos.getCol());
        pos.moveUp(100);    assertEquals(0,  pos.getRow());

        pos.moveRight(999); assertEquals(19, pos.getCol());
        pos.moveDown(999);  assertEquals(9,  pos.getRow());
    }

    // Covers: moveLeft(n), moveRight(n), moveUp(n), moveDown(n) with negative n (ignored)
    @Test
    void multiStepMoves_negativeN_noMovement() {
        CursorPosition pos = new CursorPosition(5, 5, 10, 20);
        pos.moveLeft(-3);  assertEquals(5, pos.getCol());
        pos.moveRight(-3); assertEquals(5, pos.getCol());
        pos.moveUp(-3);    assertEquals(5, pos.getRow());
        pos.moveDown(-3);  assertEquals(5, pos.getRow());
    }

    // Covers: toString
    @Test
    void toString_returnsExpectedFormat() {
        CursorPosition pos = new CursorPosition(3, 7, 10, 20);
        assertEquals("CursorPosition{row=3, col=7}", pos.toString());
    }
}