package terminal;

public class CursorPosition {
    private int col, row;

    public CursorPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }


    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void moveLeft() {
        this.col--;
    }

    public void moveRight() {
        this.col++;
    }

    public void moveUp() {
        this.row--;
    }

    public void moveDown() {
        this.row++;
    }
}
