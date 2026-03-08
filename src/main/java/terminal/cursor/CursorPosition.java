package terminal.cursor;

/**
 * Tracks the cursor's position within a terminal screen and enforces screen bounds.
 *
 * <p>All movement and position changes are clamped to the screen dimensions passed
 * at construction time, so callers never need to worry about the cursor going
 * out of bounds. Row 0 is the top of the screen; column 0 is the left edge.</p>
 */
public class CursorPosition {
    private int col, row;
    private final int maxCol;
    private final int maxRow;

    /**
     * Creates a cursor at the given position, clamped to the screen dimensions.
     *
     * @param row    initial row (clamped to 0..maxRow-1)
     * @param col    initial column (clamped to 0..maxCol-1)
     * @param maxRow number of rows on screen
     * @param maxCol number of columns on screen
     */
    public CursorPosition(int row, int col, int maxRow, int maxCol) {
        this.maxRow = maxRow;
        this.maxCol = maxCol;
        this.row = clamp(row, 0, maxRow - 1);
        this.col = clamp(col, 0, maxCol - 1);
    }

    /**
     * Creates a cursor at the given position with no bounds enforcement.
     * Kept for backward compatibility - prefer the four-argument constructor
     * when screen dimensions are known.
     *
     * @param row initial row
     * @param col initial column
     */
    public CursorPosition(int row, int col) {
        this(row, col, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    // ------ Getters / Setters ------

    /** Returns the current column. */
    public int getCol() { return col; }

    /**
     * Moves the cursor to the given column, clamped to the screen width.
     *
     * @param col the target column
     */
    public void setCol(int col) {
        this.col = clamp(col, 0, maxCol - 1);
    }

    /** Returns the current row. */
    public int getRow() { return row; }

    /**
     * Moves the cursor to the given row, clamped to the screen height.
     *
     * @param row the target row
     */
    public void setRow(int row) {
        this.row = clamp(row, 0, maxRow - 1);
    }

    /**
     * Moves the cursor to an absolute position in one call.
     * Both values are clamped to the screen bounds.
     *
     * @param row the target row
     * @param col the target column
     */
    public void setPosition(int row, int col) {
        setRow(row);
        setCol(col);
    }

    // ------ Single-step movement ------

    /** Moves the cursor one column to the left, stopping at the left edge. */
    public void moveLeft()  { setCol(col - 1); }

    /** Moves the cursor one column to the right, stopping at the right edge. */
    public void moveRight() { setCol(col + 1); }

    /** Moves the cursor one row up, stopping at the top of the screen. */
    public void moveUp()    { setRow(row - 1); }

    /** Moves the cursor one row down, stopping at the bottom of the screen. */
    public void moveDown()  { setRow(row + 1); }

    // ------ Multi-step movement ------

    /**
     * Moves the cursor left by {@code n} columns, stopping at the left edge.
     * Negative values are treated as zero - the cursor won't move right.
     *
     * @param n number of columns to move
     */
    public void moveLeft(int n)  { setCol(col - Math.max(n, 0)); }

    /**
     * Moves the cursor right by {@code n} columns, stopping at the right edge.
     * Negative values are treated as zero - the cursor won't move left.
     *
     * @param n number of columns to move
     */
    public void moveRight(int n) { setCol(col + Math.max(n, 0)); }

    /**
     * Moves the cursor up by {@code n} rows, stopping at the top of the screen.
     * Negative values are treated as zero - the cursor won't move down.
     *
     * @param n number of rows to move
     */
    public void moveUp(int n)    { setRow(row - Math.max(n, 0)); }

    /**
     * Moves the cursor down by {@code n} rows, stopping at the bottom of the screen.
     * Negative values are treated as zero - the cursor won't move up.
     *
     * @param n number of rows to move
     */
    public void moveDown(int n)  { setRow(row + Math.max(n, 0)); }

    // ------ Helpers ------

    /**
     * Clamps {@code value} so it stays within [{@code min}, {@code max}].
     * Used by every setter to keep the cursor on screen.
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public String toString() {
        return "CursorPosition{row=" + row + ", col=" + col + "}";
    }
}