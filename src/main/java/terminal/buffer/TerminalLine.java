package terminal.buffer;

/**
 * Represents a single row in the terminal grid.
 *
 * <p>Each line holds a fixed-width array of {@link CharacterCell}s. Null entries
 * mean that position hasn't been written to yet and should be treated as a blank
 * space when rendering. The {@code wrapped} flag signals that this line runs directly
 * into the next one - i.e. the cursor hit the right edge mid-line rather than a
 * natural line ending.</p>
 */
public class TerminalLine {
    private CharacterCell[] cells;
    private boolean wrapped;

    /**
     * Creates a new empty line of the given width.
     * All cells start as null (unwritten).
     *
     * @param width number of columns in this line
     */
    public TerminalLine(int width) {
        this.cells = new CharacterCell[width];
        this.wrapped = false;
    }

    /**
     * Writes a cell at the given column index, replacing whatever was there before.
     * Pass null to clear the cell back to an unwritten state.
     *
     * @param index         the column to write to
     * @param characterCell the cell to store, or null to clear
     */
    public void setCell(int index, CharacterCell characterCell) {
        this.cells[index] = characterCell;
    }

    /**
     * Returns the cell at the given column, or null if nothing has been written there.
     *
     * @param index the column to read
     * @return the cell at that position, or null if empty
     */
    public CharacterCell getCell(int index) {
        return cells[index];
    }

    /**
     * Returns the raw backing array of cells.
     * This is a direct reference - mutating it will affect the line, so handle with care.
     *
     * @return the cell array
     */
    public CharacterCell[] getCells() { return cells; }

    /**
     * Returns true if this line wraps directly into the next one.
     * A wrapped line means the cursor hit column width-1 and kept going,
     * rather than a newline being written.
     */
    public boolean isWrapped() { return wrapped; }

    /**
     * Marks or unmarks this line as wrapped.
     * Set to true when writing causes the cursor to overflow to the next row.
     *
     * @param wrapped whether this line continues onto the next
     */
    public void setWrapped(boolean wrapped) { this.wrapped = wrapped; }

    /**
     * Renders the line as a plain string. Null cells become spaces so the
     * returned string always has exactly {@code width} characters.
     *
     * @return the line content as a string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CharacterCell cell : cells) {
            // Unwritten cells render as a blank space
            if (cell == null) sb.append(" ");
            else sb.append(cell);
        }
        return sb.toString();
    }
}