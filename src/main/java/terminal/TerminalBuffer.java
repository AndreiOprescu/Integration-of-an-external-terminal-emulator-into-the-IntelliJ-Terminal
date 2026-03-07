package terminal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class TerminalBuffer {
    private List<TerminalLine> allLines;
    private CursorPosition cursor;

    private int width;
    private int height;
    private int maxSize;

    private int firstLineIndex, lastLineIndex;

    public TerminalBuffer(int width, int height, int maxSize) {
        this.width = width;
        this.height = height;
        this.maxSize = maxSize;

        this.allLines = new ArrayList<>();
        this.allLines.add(new TerminalLine(width));

        this.cursor = new CursorPosition(0, 0, height, width);

        this.firstLineIndex = 0;
        this.lastLineIndex = height;
    }

    // ------ Editing Functionality ------

    public void writeText(Character character) {
        TerminalLine line = allLines.get(this.cursor.getRow());
        line.setCell(cursor.getCol(), new CharacterCell(character));
        cursor.moveRight();

        int col = this.cursor.getCol();
        if(col >= this.width) {
            this.cursor.setCol(0);
            this.cursor.moveDown();
        }
        if(this.cursor.getRow() >= this.allLines.size()) allLines.add(new TerminalLine(this.width));
    }

    public void insertText(Character character) {
        CharacterCell characterCell = new CharacterCell(character);
        this.insertText(characterCell, cursor.getRow(), cursor.getCol());
    }

    private void insertText(CharacterCell character, int row, int col) {
        TerminalLine line = allLines.get(row);

        if(line.isWrapped()) {
            CharacterCell lastCell = line.getCell(this.width - 1);
            System.arraycopy(line.getCells(), col, line.getCells(), col+1, this.width - col - 1);
            line.setCell(col, character);
            insertText(lastCell, row + 1, 0);
        }
        else {
            CharacterCell lastCell = line.getCell(this.width - 1);
            System.arraycopy(line.getCells(), col, line.getCells(), col+1, this.width - col - 1);
            line.setCell(col, character);
            if(lastCell == null) return;

            line.setWrapped(true);
            TerminalLine newLine = new TerminalLine(this.width);
            newLine.setCell(0, lastCell);
            allLines.add(row + 1, newLine);
        }
    }

    public void fillLine(Character character) {
        TerminalLine line = allLines.get(this.cursor.getRow());
        for(int i = 0; i < this.width; i++) {
            line.setCell(i, new CharacterCell(character));
        }
    }

    public void insertEmptyLineBottom() {
        allLines.add(new TerminalLine(this.width));
    }

    public void clearEntireScreen() {
        allLines = new ArrayList<>();
        allLines.add(new TerminalLine(width));

        this.firstLineIndex = 0;
        this.lastLineIndex = this.height;
    }

    public void clearScreenAndScrollback() {
        for(int i = this.firstLineIndex; i < Math.max(this.lastLineIndex, this.allLines.size()); i++) {
            allLines.remove(this.firstLineIndex);
        }
        if(this.firstLineIndex > this.height) {
            this.firstLineIndex -= this.height;
            this.lastLineIndex -= this.height;
        }
        else {
            allLines.add(new TerminalLine(width));
            this.firstLineIndex = 0;
            this.lastLineIndex = this.height;
        }
    }

    // ------ Content Access ------

    /**
     * Returns the character at the given screen position.
     *
     * @param row screen row (0 = top of screen)
     * @param col column index
     * @return the character, or null if the cell is empty
     */
    public Character getCharacterAt(int row, int col) {
        CharacterCell cell = getCellAt(row, col);
        if (cell == null) return null;
        return cell.getCharacter();
    }

    /**
     * Returns the character at the given scrollback position.
     *
     * @param row scrollback row (0 = oldest line)
     * @param col column index
     * @return the character, or null if the cell is empty
     */
    public Character getScrollbackCharacterAt(int row, int col) {
        CharacterCell cell = getScrollbackCellAt(row, col);
        if (cell == null) return null;
        return cell.getCharacter();
    }

    /**
     * Returns the full CharacterCell (with color and style attributes) at the given screen position.
     *
     * @param row screen row (0 = top of screen)
     * @param col column index
     * @return the CharacterCell, or null if out of bounds
     */
    public CharacterCell getCellAt(int row, int col) {
        if (row < 0 || row >= height || col < 0 || col >= width) return null;
        int absoluteRow = firstLineIndex + row;
        if (absoluteRow >= allLines.size()) return null;
        return allLines.get(absoluteRow).getCell(col);
    }

    /**
     * Returns the full CharacterCell (with color and style attributes) at the given scrollback position.
     *
     * @param row scrollback row (0 = oldest line)
     * @param col column index
     * @return the CharacterCell, or null if out of bounds
     */
    public CharacterCell getScrollbackCellAt(int row, int col) {
        if (row < 0 || row >= firstLineIndex || col < 0 || col >= width) return null;
        return allLines.get(row).getCell(col);
    }

    /**
     * Returns the content of a screen line as a string.
     * Null cells are represented as spaces. Trailing spaces are trimmed.
     *
     * @param row screen row (0 = top of screen)
     * @return the line content as a string, or null if row is out of bounds
     */
    public String getScreenLineAsString(int row) {
        if (row < 0 || row >= height) return null;
        int absoluteRow = firstLineIndex + row;
        if (absoluteRow >= allLines.size()) return "";
        return allLines.get(absoluteRow).toString();
    }

    /**
     * Returns the content of a scrollback line as a string.
     * Null cells are represented as spaces. Trailing spaces are trimmed.
     *
     * @param row scrollback row (0 = oldest line)
     * @return the line content as a string, or null if row is out of bounds
     */
    public String getScrollbackLineAsString(int row) {
        if (row < 0 || row >= firstLineIndex) return null;
        return allLines.get(row).toString();
    }

    /**
     * Returns the entire visible screen as a single string, with rows separated by newlines.
     * Wrapped lines are joined without a newline between them.
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

            // Only append newline if this line does not wrap onto the next
            if (!line.isWrapped()) sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Returns the entire screen and scrollback content as a single string,
     * with rows separated by newlines. Wrapped lines are joined without a newline.
     *
     * @return the full buffer content as a string
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


    // ------ Cursor Functionality ------
    public CursorPosition getCursor() {
        return cursor;
    }

    public void setCursor(CursorPosition cursor) {
        this.cursor = cursor;
    }

    public void setCursor(int row, int col) {
        this.cursor = new CursorPosition(row, col, this.height, this.width);
    }
}
