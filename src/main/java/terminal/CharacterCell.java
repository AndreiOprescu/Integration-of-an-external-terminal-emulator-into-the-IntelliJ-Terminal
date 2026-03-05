package terminal;

public class CharacterCell {
    Character character;
    String foregroundColor;
    String backgroundColor;
    String style;

    public Character getCharacter() {
        return character;
    }

    protected void setCharacter(Character character) {
        this.character = character;
    }

    public String getForegroundColor() {
        return foregroundColor;
    }

    protected void setForegroundColor(String foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    protected void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getStyle() {
        return style;
    }

    protected void setStyle(String style) {
        this.style = style;
    }
}
