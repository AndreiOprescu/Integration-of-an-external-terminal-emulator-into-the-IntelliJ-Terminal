package terminal;

public class CursorPosition {
    private int col, row;
    private final int maxCol;
    private final int maxRow;

    public CursorPosition(int row, int col, int maxRow, int maxCol) {
        this.maxRow = maxRow;
        this.maxCol = maxCol;
        this.row = clamp(row, 0, maxRow - 1);
        this.col = clamp(col, 0, maxCol - 1);
    }

    /** Legacy constructor — no bounds enforcement (for backward compatibility). */
    public CursorPosition(int row, int col) {
        this(row, col, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    // ------ Getters / Setters ------

    public int getCol() { return col; }

    public void setCol(int col) {
        this.col = clamp(col, 0, maxCol - 1);
    }

    public int getRow() { return row; }

    public void setRow(int row) {
        this.row = clamp(row, 0, maxRow - 1);
    }

    /** Sets both row and col in one call, clamping each to screen bounds. */
    public void setPosition(int row, int col) {
        setRow(row);
        setCol(col);
    }

    // ------ Single-step movement (existing API, now bounds-safe) ------

    public void moveLeft()  { setCol(col - 1); }
    public void moveRight() { setCol(col + 1); }
    public void moveUp()    { setRow(row - 1); }
    public void moveDown()  { setRow(row + 1); }

    // ------ Multi-step movement ------

    /**
     * Moves the cursor left by {@code n} columns, clamping at column 0.
     *
     * @param n number of columns to move; negative values are ignored
     */
    public void moveLeft(int n)  { setCol(col - Math.max(n, 0)); }

    /**
     * Moves the cursor right by {@code n} columns, clamping at the last column.
     *
     * @param n number of columns to move; negative values are ignored
     */
    public void moveRight(int n) { setCol(col + Math.max(n, 0)); }

    /**
     * Moves the cursor up by {@code n} rows, clamping at row 0.
     *
     * @param n number of rows to move; negative values are ignored
     */
    public void moveUp(int n)    { setRow(row - Math.max(n, 0)); }

    /**
     * Moves the cursor down by {@code n} rows, clamping at the last row.
     *
     * @param n number of rows to move; negative values are ignored
     */
    public void moveDown(int n)  { setRow(row + Math.max(n, 0)); }

    // ------ Helpers ------

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public String toString() {
        return "CursorPosition{row=" + row + ", col=" + col + "}";
    }
}