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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(CharacterCell cell : cells) {
            if(cell == null) sb.append(" ");
            else sb.append(cell);
        }
        return sb.toString();
    }
}