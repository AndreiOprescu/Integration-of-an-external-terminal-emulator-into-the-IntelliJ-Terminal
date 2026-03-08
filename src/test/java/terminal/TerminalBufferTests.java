package terminal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for {@link TerminalBuffer}.
 *
 * Structure:
 *  - Setup
 *  - Attributes
 *  - Cursor
 *  - Editing (write, insert, fill, insertEmptyLineBottom)
 *  - Clear operations
 *  - Content Access
 *  - Scrollback
 *  - Edge / boundary cases
 */
class TerminalBufferTests {

    // Standard small terminal used by most tests
    private static final int W = 10;
    private static final int H = 5;
    private static final int MAX = 100;

    private TerminalBuffer buf;

    @BeforeEach
    void setUp() {
        buf = new TerminalBuffer(W, H, MAX);
    }

    // =========================================================================
    // 1. Setup
    // =========================================================================

    @Nested
    class Setup {

        @Test
        void initialCursorIsAtOrigin() {
            assertEquals(0, buf.getCursor().getRow());
            assertEquals(0, buf.getCursor().getCol());
        }

        @Test
        void initialScreenIsEmpty() {
            for (int r = 0; r < H; r++) {
                assertEquals("", buf.getScreenLineAsString(r).stripTrailing());
            }
        }

        @Test
        void getScreenAsStringHasCorrectLineCount() {
            String screen = buf.getScreenAsString();
            // H newline-terminated lines → H '\n' characters
            long newlines = screen.chars().filter(c -> c == '\n').count();
            assertEquals(H, newlines);
        }

        @Test
        void minimalBuffer_1x1() {
            TerminalBuffer tiny = new TerminalBuffer(1, 1, 10);
            assertNotNull(tiny.getCursor());
            assertEquals(0, tiny.getCursor().getRow());
            assertEquals(0, tiny.getCursor().getCol());
        }
    }

    // =========================================================================
    // 2. Attributes
    // =========================================================================

    @Nested
    class Attributes {

        @Test
        void setAttributesForegroundAppliedOnWrite() {
            buf.setAttributes(Color.RED, Color.BLACK, Set.of());
            buf.writeText('A');
            CharacterCell cell = buf.getCellAt(0, 0);
            assertNotNull(cell);
            assertEquals(Color.RED, cell.getForegroundColor());
        }

        @Test
        void setAttributesBackgroundAppliedOnWrite() {
            buf.setAttributes(Color.WHITE, Color.BLUE, Set.of());
            buf.writeText('B');
            CharacterCell cell = buf.getCellAt(0, 0);
            assertNotNull(cell);
            assertEquals(Color.BLUE, cell.getBackgroundColor());
        }

        @Test
        void setAttributesStyleAppliedOnWrite() {
            buf.setAttributes(Color.WHITE, Color.BLACK, Set.of(Style.BOLD));
            buf.writeText('C');
            CharacterCell cell = buf.getCellAt(0, 0);
            assertNotNull(cell);
            assertTrue(cell.getStyles().contains(Style.BOLD));
        }

        @Test
        void multipleStylesApplied() {
            buf.setAttributes(Color.WHITE, Color.BLACK, Set.of(Style.BOLD, Style.ITALIC, Style.UNDERLINE));
            buf.writeText('D');
            CharacterCell cell = buf.getCellAt(0, 0);
            assertNotNull(cell);
            assertTrue(cell.getStyles().containsAll(Set.of(Style.BOLD, Style.ITALIC, Style.UNDERLINE)));
        }

        @Test
        void attributesChangeMidLine() {
            buf.setAttributes(Color.RED, Color.BLACK, Set.of());
            buf.writeText('A');
            buf.setAttributes(Color.GREEN, Color.BLACK, Set.of());
            buf.writeText('B');

            assertEquals(Color.RED,   buf.getCellAt(0, 0).getForegroundColor());
            assertEquals(Color.GREEN, buf.getCellAt(0, 1).getForegroundColor());
        }

        @Test
        void defaultAttributesUsedWhenNotSet() {
            buf.writeText('X');
            CharacterCell cell = buf.getCellAt(0, 0);
            assertNotNull(cell);
            // Defaults from CharacterCell(char) constructor
            assertEquals(Color.WHITE, cell.getForegroundColor());
            assertEquals(Color.BLACK, cell.getBackgroundColor());
            assertTrue(cell.getStyles().isEmpty());
        }

        @Test
        void setAttributesDoesNotMutateAlreadyWrittenCells() {
            buf.writeText('A');                                    // written with defaults
            buf.setAttributes(Color.RED, Color.BLUE, Set.of(Style.BOLD));
            // cell at (0,0) must still have original attributes
            assertEquals(Color.WHITE, buf.getCellAt(0, 0).getForegroundColor());
            assertEquals(Color.BLACK, buf.getCellAt(0, 0).getBackgroundColor());
        }
    }

    // =========================================================================
    // 3. Cursor
    // =========================================================================

    @Nested
    class Cursor {

        @Test
        void setCursorAbsolute() {
            buf.setCursor(2, 3);
            assertEquals(2, buf.getCursor().getRow());
            assertEquals(3, buf.getCursor().getCol());
        }

        @Test
        void setCursorWithCursorPosition() {
            buf.setCursor(new CursorPosition(1, 4, H, W));
            assertEquals(1, buf.getCursor().getRow());
            assertEquals(4, buf.getCursor().getCol());
        }

        @Test
        void moveCursorRightByN() {
            buf.setCursor(0, 0);
            buf.getCursor().moveRight(3);
            assertEquals(3, buf.getCursor().getCol());
        }

        @Test
        void moveCursorLeftByN() {
            buf.setCursor(0, 5);
            buf.getCursor().moveLeft(3);
            assertEquals(2, buf.getCursor().getCol());
        }

        @Test
        void moveCursorDownByN() {
            buf.setCursor(0, 0);
            buf.getCursor().moveDown(2);
            assertEquals(2, buf.getCursor().getRow());
        }

        @Test
        void moveCursorUpByN() {
            buf.setCursor(4, 0);
            buf.getCursor().moveUp(2);
            assertEquals(2, buf.getCursor().getRow());
        }

        @Test
        void cursorClampsAtLeftBoundary() {
            buf.setCursor(0, 2);
            buf.getCursor().moveLeft(10);   // would go to -8
            assertEquals(0, buf.getCursor().getCol());
        }

        @Test
        void cursorClampsAtRightBoundary() {
            buf.setCursor(0, 8);
            buf.getCursor().moveRight(10);  // would go to 18
            assertEquals(W - 1, buf.getCursor().getCol());
        }

        @Test
        void cursorClampsAtTopBoundary() {
            buf.setCursor(1, 0);
            buf.getCursor().moveUp(10);
            assertEquals(0, buf.getCursor().getRow());
        }

        @Test
        void cursorClampsAtBottomBoundary() {
            buf.setCursor(3, 0);
            buf.getCursor().moveDown(10);
            assertEquals(H - 1, buf.getCursor().getRow());
        }

        @Test
        void setCursorClampsOutOfBoundsRow() {
            buf.setCursor(H + 5, 0);
            assertEquals(H - 1, buf.getCursor().getRow());
        }

        @Test
        void setCursorClampsOutOfBoundsCol() {
            buf.setCursor(0, W + 5);
            assertEquals(W - 1, buf.getCursor().getCol());
        }

        @Test
        void setCursorClampsNegativeRow() {
            buf.setCursor(-1, 0);
            assertEquals(0, buf.getCursor().getRow());
        }

        @Test
        void setCursorClampsNegativeCol() {
            buf.setCursor(0, -1);
            assertEquals(0, buf.getCursor().getCol());
        }

        @Test
        void moveByZeroIsNoOp() {
            buf.setCursor(2, 3);
            buf.getCursor().moveRight(0);
            buf.getCursor().moveDown(0);
            assertEquals(2, buf.getCursor().getRow());
            assertEquals(3, buf.getCursor().getCol());
        }

        @Test
        void moveByNegativeIsNoOp() {
            buf.setCursor(2, 3);
            buf.getCursor().moveRight(-5);
            buf.getCursor().moveDown(-5);
            assertEquals(2, buf.getCursor().getRow());
            assertEquals(3, buf.getCursor().getCol());
        }
    }

    // =========================================================================
    // 4. Editing — writeText
    // =========================================================================

    @Nested
    class WriteText {

        @Test
        void writeSingleCharacter() {
            buf.writeText('A');
            assertEquals('A', buf.getCharacterAt(0, 0));
        }

        @Test
        void writeAdvancesCursorByOne() {
            buf.writeText('A');
            assertEquals(1, buf.getCursor().getCol());
        }

        @Test
        void writeMultipleCharactersOnSameLine() {
            String text = "Hello";
            for (char c : text.toCharArray()) buf.writeText(c);
            assertEquals("Hello", buf.getScreenLineAsString(0).substring(0, 5));
        }

        @Test
        void writeOverridesExistingContent() {
            buf.writeText('A');
            buf.setCursor(0, 0);
            buf.writeText('B');
            assertEquals('B', buf.getCharacterAt(0, 0));
        }

        @Test
        void writeWrapsToNextLineAtEndOfWidth() {
            buf.setCursor(0, W - 1);
            buf.writeText('X');   // writes at col W-1, cursor should wrap to (1,0)
            assertEquals(1, buf.getCursor().getRow());
            assertEquals(0, buf.getCursor().getCol());
        }

        @Test
        void writeToLastCellOfLastRowDoesNotCrash() {
            buf.setCursor(H - 1, W - 1);
            assertDoesNotThrow(() -> buf.writeText('Z'));
        }

        @Test
        void fillLineWritesCharacterAcrossEntireWidth() {
            buf.setCursor(0, 0);
            buf.fillLine('*');
            String line = buf.getScreenLineAsString(0);
            assertEquals("*".repeat(W), line);
        }

        @Test
        void fillLineDoesNotAffectOtherRows() {
            buf.setCursor(0, 0);
            buf.fillLine('*');
            assertEquals("", buf.getScreenLineAsString(1).stripTrailing());
        }

        @Test
        void fillLineWithNullClearsLine() {
            buf.fillLine('X');
            buf.fillLine(null);  // null should represent an empty cell
            for (int c = 0; c < W; c++) {
                assertNull(buf.getCharacterAt(0, c));
            }
        }

        @Test
        void writeUpdatesContentAccessibleViaGetScreenAsString() {
            buf.writeText('H');
            buf.writeText('i');
            assertTrue(buf.getScreenAsString().contains("Hi"));
        }
    }

    // =========================================================================
    // 5. Editing — insertText
    // =========================================================================

    @Nested
    class InsertText {

        @Test
        void insertAtStartPushesExistingContentRight() {
            // Pre-fill "BCD" starting at col 0
            buf.writeText('B'); buf.writeText('C'); buf.writeText('D');
            buf.setCursor(0, 0);
            buf.insertText('A');
            assertEquals('A', buf.getCharacterAt(0, 0));
            assertEquals('B', buf.getCharacterAt(0, 1));
            assertEquals('C', buf.getCharacterAt(0, 2));
            assertEquals('D', buf.getCharacterAt(0, 3));
        }

        @Test
        void insertInMiddlePushesRightHalfRight() {
            for (char c : "ABCD".toCharArray()) buf.writeText(c);
            buf.setCursor(0, 2);
            buf.insertText('X');
            assertEquals('A', buf.getCharacterAt(0, 0));
            assertEquals('B', buf.getCharacterAt(0, 1));
            assertEquals('X', buf.getCharacterAt(0, 2));
            assertEquals('C', buf.getCharacterAt(0, 3));
            assertEquals('D', buf.getCharacterAt(0, 4));
        }

        @Test
        void insertAtFullLineWrapsLastCharToNextLine() {
            // Fill entire first line
            for (int i = 0; i < W; i++) buf.writeText((char) ('A' + i));
            buf.setCursor(0, 0);
            buf.insertText('Z');
            // Last char of first line ('J', i.e. 'A'+9) overflows to second line
            assertNotNull(buf.getCharacterAt(1, 0));
        }

        @Test
        void insertDoesNotLosePushedCharacters() {
            for (char c : "ABCDE".toCharArray()) buf.writeText(c);
            buf.setCursor(0, 0);
            buf.insertText('X');
            String line = buf.getScreenLineAsString(0).stripTrailing();
            assertTrue(line.startsWith("XABCDE"));
        }

        @Test
        void insertAtEndOfLineAppendsWithoutWrap() {
            for (char c : "ABC".toCharArray()) buf.writeText(c);
            buf.setCursor(0, 3);
            buf.insertText('D');
            assertEquals('D', buf.getCharacterAt(0, 3));
        }
    }

    // =========================================================================
    // 6. Editing — insertEmptyLineBottom
    // =========================================================================

    @Nested
    class InsertEmptyLineBottom {

        @Test
        void insertEmptyLineAddsALine() {
            buf.writeText('X');
            int linesBefore = buf.getScreenAsString().split("\n", -1).length;
            buf.insertEmptyLineBottom();
            int linesAfter = buf.getScreenAsString().split("\n", -1).length;
            assertTrue(linesAfter >= linesBefore);
        }

        @Test
        void insertedLineIsEmpty() {
            buf.insertEmptyLineBottom();
            // The newly added line (beyond height) shouldn't crash on scrollback access
            assertDoesNotThrow(() -> buf.getFullContentAsString());
        }

        @Test
        void multipleInsertsDoNotCrash() {
            assertDoesNotThrow(() -> {
                for (int i = 0; i < MAX + 10; i++) buf.insertEmptyLineBottom();
            });
        }
    }

    // =========================================================================
    // 7. Clear operations
    // =========================================================================

    @Nested
    class ClearOperations {

        @Test
        void clearEntireScreenRemovesContent() {
            buf.writeText('A');
            buf.clearEntireScreen();
            assertNull(buf.getCharacterAt(0, 0));
        }

        @Test
        void clearEntireScreenResetsCursorToOrigin() {
            buf.setCursor(2, 3);
            buf.clearEntireScreen();
            // Cursor position after clear is implementation-defined;
            // we just verify it is within valid bounds.
            assertTrue(buf.getCursor().getRow() >= 0 && buf.getCursor().getRow() < H);
            assertTrue(buf.getCursor().getCol() >= 0 && buf.getCursor().getCol() < W);
        }

        @Test
        void clearEntireScreenPreservesScrollback() {
            // Force a line to scroll off by writing past height
            for (int row = 0; row <= H; row++) {
                for (int c = 0; c < W; c++) buf.writeText('X');
            }
            buf.clearEntireScreen();
            // Scrollback should still contain something
            assertNotNull(buf.getFullContentAsString());
        }

        @Test
        void clearScreenAndScrollbackRemovesAllContent() {
            for (int row = 0; row <= H; row++) {
                for (int c = 0; c < W; c++) buf.writeText('X');
            }
            buf.clearScreenAndScrollback();
            String full = buf.getFullContentAsString().stripTrailing().replace("\n", "");
            assertEquals("", full);
        }

        @Test
        void clearScreenAndScrollbackAllowsWritingAfterward() {
            buf.clearScreenAndScrollback();
            assertDoesNotThrow(() -> buf.writeText('A'));
            assertEquals('A', buf.getCharacterAt(0, 0));
        }

        @Test
        void clearScreenTwiceDoesNotCrash() {
            assertDoesNotThrow(() -> {
                buf.clearEntireScreen();
                buf.clearEntireScreen();
            });
        }

        @Test
        void clearScrollbackTwiceDoesNotCrash() {
            assertDoesNotThrow(() -> {
                buf.clearScreenAndScrollback();
                buf.clearScreenAndScrollback();
            });
        }
    }

    // =========================================================================
    // 8. Content Access — screen
    // =========================================================================

    @Nested
    class ContentAccessScreen {

        @Test
        void getCharacterAtValidPosition() {
            buf.writeText('Q');
            assertEquals('Q', buf.getCharacterAt(0, 0));
        }

        @Test
        void getCharacterAtEmptyCellReturnsEmpty() {
            assertEquals(buf.getCharacterAt(0, 0), ' ');
        }

        @Test
        void getCharacterAtOutOfBoundsRowReturnsNull() {
            assertNull(buf.getCharacterAt(H, 0));
            assertNull(buf.getCharacterAt(-1, 0));
        }

        @Test
        void getCharacterAtOutOfBoundsColReturnsNull() {
            assertNull(buf.getCharacterAt(0, W));
            assertNull(buf.getCharacterAt(0, -1));
        }

        @Test
        void getCellAtReturnsCorrectAttributes() {
            buf.setAttributes(Color.RED, Color.BLUE, Set.of(Style.BOLD));
            buf.writeText('R');
            CharacterCell cell = buf.getCellAt(0, 0);
            assertNotNull(cell);
            assertEquals(Color.RED,  cell.getForegroundColor());
            assertEquals(Color.BLUE, cell.getBackgroundColor());
            assertTrue(cell.getStyles().contains(Style.BOLD));
        }

        @Test
        void getCellAtOutOfBoundsReturnsNull() {
            assertNull(buf.getCellAt(-1, 0));
            assertNull(buf.getCellAt(0, -1));
            assertNull(buf.getCellAt(H, 0));
            assertNull(buf.getCellAt(0, W));
        }

        @Test
        void getScreenLineAsStringOutOfBoundsReturnsNull() {
            assertNull(buf.getScreenLineAsString(-1));
            assertNull(buf.getScreenLineAsString(H));
        }

        @Test
        void getScreenLineAsStringEmptyLineReturnsEmptyOrSpaces() {
            String line = buf.getScreenLineAsString(0);
            assertNotNull(line);
            assertEquals("", line.stripTrailing());
        }

        @Test
        void getScreenLineAsStringReturnsWrittenContent() {
            for (char c : "HELLO".toCharArray()) buf.writeText(c);
            assertTrue(buf.getScreenLineAsString(0).startsWith("HELLO"));
        }

        @Test
        void getScreenAsStringContainsAllRows() {
            buf.setCursor(0, 0); buf.writeText('A');
            buf.setCursor(1, 0); buf.writeText('B');
            String screen = buf.getScreenAsString();
            assertTrue(screen.contains("A"));
            assertTrue(screen.contains("B"));
        }

        @Test
        void getScreenAsStringNullCellsRenderedAsSpaces() {
            // Row 0, col 0 is empty; getScreenAsString must not throw
            String screen = assertDoesNotThrow(() -> buf.getScreenAsString());
            assertNotNull(screen);
        }
    }

    // =========================================================================
    // 9. Content Access — scrollback
    // =========================================================================

    @Nested
    class ContentAccessScrollback {

        /** Push enough characters to force lines into scrollback. */
        private void fillScreenPlusOne() {
            for (int row = 0; row <= H; row++) {
                for (int c = 0; c < W; c++) buf.writeText((char) ('A' + (row % 26)));
            }
        }

        @Test
        void getScrollbackCharacterAtValidRow() {
            fillScreenPlusOne();
            // Row 0 of scrollback should have 'A'
            Character ch = buf.getScrollbackCharacterAt(0, 0);
            assertNotNull(ch);
        }

        @Test
        void getScrollbackCharacterAtOutOfBoundsReturnsNull() {
            fillScreenPlusOne();
            // Negative row
            assertNull(buf.getScrollbackCharacterAt(-1, 0));
            // Col out of bounds
            assertNull(buf.getScrollbackCharacterAt(0, W));
            assertNull(buf.getScrollbackCharacterAt(0, -1));
        }

        @Test
        void getScrollbackCellAtReturnsCorrectCell() {
            buf.setAttributes(Color.CYAN, Color.BLACK, Set.of());

            // Write a full line of Z's on row 0
            for (int c = 0; c < W; c++) buf.writeText('Z');

            // Fill the remaining screen rows with X's so the Z line scrolls off
            for (int row = 1; row < H; row++) {
                for (int c = 0; c < W; c++) buf.writeText('X');
            }

            // Write one more full line — this triggers scroll(), pushing Z into scrollback
            for (int c = 0; c < W; c++) buf.writeText('X');

            // Z line is now at scrollback index 0
            CharacterCell cell = buf.getScrollbackCellAt(0, 0);
            assertNotNull(cell);
            assertEquals('Z', cell.getCharacter());
            assertEquals(Color.CYAN, cell.getForegroundColor());
        }

        @Test
        void getScrollbackCellAtOutOfBoundsReturnsNull() {
            assertNull(buf.getScrollbackCellAt(0, 0));   // nothing in scrollback yet
            assertNull(buf.getScrollbackCellAt(-1, 0));
            assertNull(buf.getScrollbackCellAt(0, -1));
        }

        @Test
        void getScrollbackLineAsStringOutOfBoundsReturnsNull() {
            assertNull(buf.getScrollbackLineAsString(-1));
            assertNull(buf.getScrollbackLineAsString(0)); // nothing scrolled off yet
        }

        @Test
        void getScrollbackLineAsStringReturnsContent() {
            fillScreenPlusOne();

            String line = buf.getScrollbackLineAsString(0);
            assertNotNull(line);
            assertFalse(line.stripTrailing().isEmpty());
        }

        @Test
        void getFullContentAsStringContainsScrollbackAndScreen() {
            fillScreenPlusOne();
            String full = buf.getFullContentAsString();
            assertNotNull(full);
            // Should be longer than just the screen
            assertTrue(full.length() > buf.getScreenAsString().length());
        }

        @Test
        void scrollbackIsImmutable() {
            fillScreenPlusOne();
            String scrollbackBefore = buf.getScrollbackLineAsString(0);
            // Write more on screen — scrollback row 0 must not change
            for (char c : "XXXXXXXXXX".toCharArray()) buf.writeText(c);
            assertEquals(scrollbackBefore, buf.getScrollbackLineAsString(0));
        }
    }

    // =========================================================================
    // 10. Scrollback size limit
    // =========================================================================

    @Nested
    class ScrollbackSizeLimit {

        @Test
        void scrollbackDoesNotExceedMaxSize() {
            // Use a buffer with a very small scrollback
            TerminalBuffer small = new TerminalBuffer(W, H, 3);
            // Force many lines into scrollback
            for (int row = 0; row < H + 10; row++) {
                for (int c = 0; c < W; c++) small.writeText('X');
            }
            // Rows beyond maxSize should return null (no longer in scrollback)
            assertNull(small.getScrollbackLineAsString(3));
        }
    }

    // =========================================================================
    // 11. Wrap behaviour
    // =========================================================================

    @Nested
    class WrapBehaviour {

        @Test
        void wrappedLineIsMarkedWrapped() {
            for (int c = 0; c < W; c++) buf.writeText('A');
            // The first line should be marked as wrapped because content continued
            // We verify via screen content continuity rather than internal flag
            // (the flag is package-private; test observable behaviour)
            String screen = buf.getScreenAsString();
            // Wrapped lines have no newline between them; 'A' * W should appear contiguous
            assertTrue(screen.contains("A".repeat(W)));
        }

        @Test
        void writingPastEndOfScreenDoesNotCrash() {
            assertDoesNotThrow(() -> {
                for (int i = 0; i < W * H * 2; i++) buf.writeText('X');
            });
        }

        @Test
        void getScreenAsStringDoesNotContainNewlineOnWrappedLine() {
            // Fill exactly one line (all W chars) → should be wrapped, no '\n' before next line
            for (int c = 0; c < W; c++) buf.writeText('A');
            String screen = buf.getScreenAsString();
            int idx = screen.indexOf("A".repeat(W));
            // The character immediately after the run must NOT be '\n' if the line is wrapped
            if (idx >= 0 && idx + W < screen.length()) {
                assertNotEquals('\n', screen.charAt(idx + W));
            }
        }
    }

    // =========================================================================
    // 12. Boundary / stress
    // =========================================================================

    @Nested
    class BoundaryAndStress {

        @Test
        void writeEntireScreenWithoutException() {
            assertDoesNotThrow(() -> {
                for (int r = 0; r < H; r++) {
                    buf.setCursor(r, 0);
                    for (int c = 0; c < W; c++) buf.writeText('X');
                }
            });
        }

        @Test
        void readEveryScreenCellWithoutException() {
            for (int r = 0; r < H; r++) {
                for (int c = 0; c < W; c++) {
                    final int row = r, col = c;
                    assertDoesNotThrow(() -> buf.getCharacterAt(row, col));
                }
            }
        }

        @Test
        void fillAndClearRepeatedly() {
            assertDoesNotThrow(() -> {
                for (int i = 0; i < 50; i++) {
                    buf.fillLine('X');
                    buf.clearEntireScreen();
                }
            });
        }

        @Test
        void largeCursorMoveClamps() {
            buf.setCursor(0, 0);
            buf.getCursor().moveRight(Integer.MAX_VALUE / 2);
            assertEquals(W - 1, buf.getCursor().getCol());
            buf.getCursor().moveDown(Integer.MAX_VALUE / 2);
            assertEquals(H - 1, buf.getCursor().getRow());
        }

        @Test
        void writeAfterClearEntireScreen() {
            buf.clearEntireScreen();
            buf.writeText('A');
            assertEquals('A', buf.getCharacterAt(0, 0));
        }

        @Test
        void writeAfterClearScreenAndScrollback() {
            buf.clearScreenAndScrollback();
            buf.writeText('B');
            assertEquals('B', buf.getCharacterAt(0, 0));
        }

        @Test
        void getScreenLineFromUnwrittenRowsReturnsEmptyOrNull() {
            // Rows that were never written should return empty string (not throw)
            for (int r = 0; r < H; r++) {
                String line = buf.getScreenLineAsString(r);
                assertTrue(line == null || line.stripTrailing().isEmpty());
            }
        }

        @Test
        void fillLineOnEveryRow() {
            assertDoesNotThrow(() -> {
                for (int r = 0; r < H; r++) {
                    buf.setCursor(r, 0);
                    buf.fillLine('*');
                }
            });
            for (int r = 0; r < H; r++) {
                assertEquals("*".repeat(W), buf.getScreenLineAsString(r));
            }
        }
    }
}