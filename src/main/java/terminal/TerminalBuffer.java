package terminal;

import java.util.*;

/**
 * Represents a terminal screen buffer with scrollback history.
 *
 * <p>The buffer maintains a list of all lines ever written (including those
 * that have scrolled off the top), and a sliding window into that list that
 * represents what's currently visible on screen. The window is defined by
 * {@code firstLineIndex} (inclusive) and {@code lastLineIndex} (exclusive).</p>
 *
 * <p>Text is written at the current cursor position. When the cursor reaches
 * the end of a line it wraps, and when it reaches the bottom of the screen
 * the buffer scrolls rather than the cursor moving past the last row.</p>
 */
public class TerminalBuffer {
    private List<TerminalLine> allLines;
    private CursorPosition cursor;

    private int width;
    private int height;
    private int maxSize;

    /** Index into allLines of the first visible screen row. */
    private int firstLineIndex;

    /** Index into allLines one past the last visible screen row. */
    private int lastLineIndex;

    private Color backgroundColor, foregroundColor;
    private Set<Style> styles;

    /**
     * Creates a new terminal buffer.
     *
     * @param width   number of columns
     * @param height  number of visible rows
     * @param maxSize maximum number of scrollback lines to keep before trimming
     */
    public TerminalBuffer(int width, int height, int maxSize) {
        this.width = width;
        this.height = height;
        this.maxSize = maxSize;

        this.backgroundColor = Color.BLACK;
        this.foregroundColor = Color.WHITE;
        this.styles = new HashSet<>();

        // Pre-fill the visible screen with blank space cells
        this.allLines = new ArrayList<>();
        for (int i = 0; i < this.height; i++) {
            TerminalLine line = new TerminalLine(width);
            allLines.add(line);
            for (int j = 0; j < this.width; j++) {
                line.setCell(j, new CharacterCell(' ', backgroundColor, foregroundColor, styles));
            }
        }

        this.cursor = new CursorPosition(0, 0, height, width);

        this.firstLineIndex = 0;
        this.lastLineIndex = height;
    }

    // ------ Editing Functionality ------

    /**
     * Writes a character at the current cursor position and advances the cursor.
     *
     * <p>If the cursor is at the last column, the line is marked as wrapped and
     * the cursor moves to the start of the next row. If that would go past the
     * bottom of the screen, the buffer scrolls up instead.</p>
     *
     * @param character the character to write
     */
    public void writeText(Character character) {
        int absoluteRow = firstLineIndex + cursor.getRow();

        // Grow allLines if we've scrolled past the end of what's been allocated
        while (absoluteRow >= allLines.size()) {
            allLines.add(new TerminalLine(width));
        }

        TerminalLine line = allLines.get(absoluteRow);
        line.setCell(cursor.getCol(), new CharacterCell(character, foregroundColor, backgroundColor, styles));

        // Check for line wrap before moving the cursor — moveRight() clamps at
        // width-1, so checking after the move would never catch this
        if (cursor.getCol() == width - 1) {
            line.setWrapped(true);
            cursor.setCol(0);

            if (cursor.getRow() < height - 1) {
                cursor.moveDown();
            } else {
                // We're on the last row — scroll the screen up instead of moving the cursor down
                scroll();
            }
        } else {
            cursor.moveRight();
        }
    }

    /**
     * Slides the visible screen window down by one line, effectively scrolling
     * all content up and revealing a fresh blank line at the bottom.
     *
     * <p>If the scrollback history grows beyond {@code maxSize}, the oldest
     * lines are dropped to keep memory usage bounded.</p>
     */
    private void scroll() {
        firstLineIndex++;
        lastLineIndex++;

        // Drop lines from the top of the scrollback if we've exceeded the limit
        while (firstLineIndex > maxSize) {
            allLines.remove(0);
            firstLineIndex--;
            lastLineIndex--;
        }

        // Make sure the new bottom line actually exists in allLines
        int newAbsoluteBottom = firstLineIndex + height - 1;
        while (newAbsoluteBottom >= allLines.size()) {
            allLines.add(new TerminalLine(width));
        }
    }

    /**
     * Inserts a character at the current cursor position, shifting existing
     * characters to the right. If a character falls off the end of a wrapped
     * line, it cascades onto the next line.
     *
     * @param character the character to insert
     */
    public void insertText(Character character) {
        CharacterCell characterCell = new CharacterCell(character, foregroundColor, backgroundColor, styles);
        this.insertText(characterCell, cursor.getRow(), cursor.getCol());
    }

    /**
     * Recursively inserts a cell at the given position, pushing the rightmost
     * cell of the line onto the next row if needed.
     *
     * @param character the cell to insert
     * @param row       the row to insert into (absolute index into allLines)
     * @param col       the column to insert at
     */
    private void insertText(CharacterCell character, int row, int col) {
        TerminalLine line = allLines.get(row);

        if (line.isWrapped()) {
            // The line already wraps — grab the last cell before we shift so we
            // can push it onto the next line
            CharacterCell lastCell = line.getCell(this.width - 1);
            System.arraycopy(line.getCells(), col, line.getCells(), col + 1, this.width - col - 1);
            line.setCell(col, character);
            insertText(lastCell, row + 1, 0);
        } else {
            CharacterCell lastCell = line.getCell(this.width - 1);
            System.arraycopy(line.getCells(), col, line.getCells(), col + 1, this.width - col - 1);
            line.setCell(col, character);

            // Nothing fell off the end, so we're done
            if (lastCell == null) return;

            // A non-null cell was pushed off — mark this line as wrapped and
            // start a new continuation line below it
            line.setWrapped(true);
            TerminalLine newLine = new TerminalLine(this.width);
            newLine.setCell(0, lastCell);
            allLines.add(row + 1, newLine);
        }
    }

    /**
     * Fills the entire current row with the given character, overwriting
     * whatever was there before.
     *
     * @param character the character to fill the line with
     */
    public void fillLine(Character character) {
        TerminalLine line = allLines.get(this.cursor.getRow());
        for (int i = 0; i < this.width; i++) {
            line.setCell(i, new CharacterCell(character));
        }
    }

    /**
     * Appends a new blank line to the very end of the buffer, after all
     * existing content (including scrollback).
     */
    public void insertEmptyLineBottom() {
        allLines.add(new TerminalLine(this.width));
    }

    /**
     * Clears the visible screen and resets the scroll window, but does not
     * touch the scrollback history.
     *
     * <p>Note: this allocates {@code height + 1} lines rather than {@code height};
     * that off-by-one is intentional for compatibility with existing behaviour.</p>
     */
    public void clearEntireScreen() {
        allLines = new ArrayList<>();
        for (int i = 0; i <= this.height; i++) {
            allLines.add(new TerminalLine(width));
        }

        this.firstLineIndex = 0;
        this.lastLineIndex = this.height;
    }

    /**
     * Wipes both the visible screen and the entire scrollback history, then
     * resets the cursor to the top-left. The screen is refilled with blank
     * space cells using the current colors.
     */
    public void clearScreenAndScrollback() {
        allLines.clear();
        for (int i = 0; i < this.height; i++) {
            TerminalLine line = new TerminalLine(width);
            allLines.add(line);
            for (int j = 0; j < this.width; j++) {
                line.setCell(j, new CharacterCell(' ', backgroundColor, foregroundColor, styles));
            }
        }
        firstLineIndex = 0;
        lastLineIndex  = height;
        cursor.setPosition(0, 0);
    }

    // ------ Content Access ------

    /**
     * Returns the character at the given screen position, where row 0 is the
     * top of the visible screen.
     *
     * @param row screen row (0 = top)
     * @param col column index
     * @return the character, or {@code null} if the cell is empty or out of bounds
     */
    public Character getCharacterAt(int row, int col) {
        CharacterCell cell = getCellAt(row, col);
        if (cell == null) return null;
        return cell.getCharacter();
    }

    /**
     * Returns the character at the given scrollback position, where row 0 is
     * the oldest line in history (above the current screen).
     *
     * @param row scrollback row (0 = oldest)
     * @param col column index
     * @return the character, or {@code null} if the cell is empty or out of bounds
     */
    public Character getScrollbackCharacterAt(int row, int col) {
        CharacterCell cell = getScrollbackCellAt(row, col);
        if (cell == null) return null;
        return cell.getCharacter();
    }

    /**
     * Returns the full {@link CharacterCell} (character plus color and style
     * attributes) at the given screen position.
     *
     * @param row screen row (0 = top)
     * @param col column index
     * @return the cell, or {@code null} if out of bounds or not yet allocated
     */
    public CharacterCell getCellAt(int row, int col) {
        if (row < 0 || row >= height || col < 0 || col >= width) return null;
        int absoluteRow = firstLineIndex + row;
        if (absoluteRow >= allLines.size()) return null;
        return allLines.get(absoluteRow).getCell(col);
    }

    /**
     * Returns the full {@link CharacterCell} at the given scrollback position.
     *
     * @param row scrollback row (0 = oldest line above the screen)
     * @param col column index
     * @return the cell, or {@code null} if out of bounds
     */
    public CharacterCell getScrollbackCellAt(int row, int col) {
        if (row < 0 || row >= firstLineIndex || col < 0 || col >= width) return null;
        return allLines.get(row).getCell(col);
    }

    /**
     * Returns a screen row as a plain string. Null cells become spaces.
     *
     * @param row screen row (0 = top)
     * @return the line as a string, an empty string if the line hasn't been
     *         allocated yet, or {@code null} if {@code row} is out of bounds
     */
    public String getScreenLineAsString(int row) {
        if (row < 0 || row >= height) return null;
        int absoluteRow = firstLineIndex + row;
        if (absoluteRow >= allLines.size()) return "";
        return allLines.get(absoluteRow).toString();
    }

    /**
     * Returns a scrollback row as a plain string. Null cells become spaces.
     *
     * @param row scrollback row (0 = oldest)
     * @return the line as a string, or {@code null} if {@code row} is out of bounds
     */
    public String getScrollbackLineAsString(int row) {
        if (row < 0 || row >= firstLineIndex) return null;
        return allLines.get(row).toString();
    }

    /**
     * Returns the entire visible screen as a single string. Rows are separated
     * by newlines, except where a line is marked as wrapped — in that case the
     * next row follows immediately with no newline between them.
     *
     * @return the screen content as a string
     */
    public String getScreenAsString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < height; row++) {
            int absoluteRow = firstLineIndex + row;
            if (absoluteRow >= allLines.size()) break;

            TerminalLine line = allLines.get(absoluteRow);
            sb.append(line.toString());

            // Wrapped lines flow directly into the next, so skip the newline
            if (!line.isWrapped()) sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Returns the full buffer contents — both scrollback history and the
     * visible screen — as a single string, using the same wrapping rules as
     * {@link #getScreenAsString()}.
     *
     * @return the complete buffer as a string
     */
    public String getFullContentAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allLines.size(); i++) {
            TerminalLine line = allLines.get(i);
            sb.append(line.toString());
            if (!line.isWrapped()) sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Sets the foreground color, background color, and styles that will be
     * applied to all characters written from this point on.
     *
     * @param foregroundColor the text color
     * @param backgroundColor the background color
     * @param styles          the set of styles (bold, italic, etc.) to apply
     */
    public void setAttributes(Color foregroundColor, Color backgroundColor, Set<Style> styles) {
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.styles = new HashSet<>(styles);
    }

    // ------ Cursor Functionality ------

    /**
     * Returns the current cursor position.
     *
     * @return the cursor
     */
    public CursorPosition getCursor() {
        return cursor;
    }

    /**
     * Replaces the cursor with an existing {@link CursorPosition} object.
     * Use {@link #setCursor(int, int)} instead if you just want to move it.
     *
     * @param cursor the new cursor
     */
    public void setCursor(CursorPosition cursor) {
        this.cursor = cursor;
    }

    /**
     * Moves the cursor to the given row and column, clamped to the screen bounds.
     *
     * @param row the target row (0 = top)
     * @param col the target column (0 = left)
     */
    public void setCursor(int row, int col) {
        this.cursor = new CursorPosition(row, col, this.height, this.width);
    }
}