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

        this.cursor = new CursorPosition(0, 0);
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
            System.arraycopy(line.getCells(), col, line.getCells(), col+1, this.width - col);
            line.setCell(col, character);
            insertText(lastCell, row + 1, 0);
        }
        else {
            CharacterCell lastCell = line.getCell(this.width - 1);
            System.arraycopy(line.getCells(), col, line.getCells(), col+1, this.width - col);
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

}
