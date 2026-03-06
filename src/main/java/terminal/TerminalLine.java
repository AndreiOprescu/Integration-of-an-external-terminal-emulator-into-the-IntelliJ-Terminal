package terminal;

public class TerminalLine {
    private CharacterCell[] cells;
    private boolean wrapped;

    public TerminalLine(int width) {
        this.cells = new CharacterCell[width];
        this.wrapped = false;
    }


    public void setCell(int index, CharacterCell characterCell) {
        this.cells[index] = characterCell;
    }

    public CharacterCell getCell(int index) {
        return cells[index];
    }
    public CharacterCell[] getCells() { return cells; }
    public boolean isWrapped() { return wrapped; }
    public void setWrapped(boolean wrapped) { this.wrapped = wrapped; }
}