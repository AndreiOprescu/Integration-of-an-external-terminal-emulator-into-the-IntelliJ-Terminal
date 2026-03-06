package terminal;

/**
 * Represents a single character cell in a terminal text buffer.
 *
 * <p>Each cell occupies one position in the terminal's grid and holds a character
 * along with its visual attributes: foreground color, background color, and text style.
 * Empty cells (no character written) have a {@code null} character value.</p>
 *
 * <p>Attributes follow the terminal's 16-color model and style flags (e.g., bold,
 * italic, underline). Setters are {@code protected} to restrict direct mutation to
 * the {@code terminal} package, ensuring the buffer remains the sole writer.</p>
 *
 * @see TerminalBuffer
 */
public class CharacterCell {

    /** The character stored in this cell, or {@code null} if the cell is empty. */
    Character character;

    /**
     * The foreground (text) color for this cell.
     * Valid values are {@code "default"} or one of the 16 standard terminal color names.
     */
    String foregroundColor;

    /**
     * The background color for this cell.
     * Valid values are {@code "default"} or one of the 16 standard terminal color names.
     */
    String backgroundColor;

    /**
     * The style flags applied to this cell's character (e.g., {@code "bold"},
     * {@code "italic"}, {@code "underline"}, or a combined representation).
     */
    String style;

    /**
     * Returns the character stored in this cell.
     *
     * @return the cell's character, or {@code null} if the cell is empty
     */
    public Character getCharacter() {
        return character;
    }

    /**
     * Sets the character for this cell.
     *
     * @param character the character to store, or {@code null} to mark the cell as empty
     */
    protected void setCharacter(Character character) {
        this.character = character;
    }

    /**
     * Returns the foreground color of this cell.
     *
     * @return the foreground color string (e.g., {@code "default"}, {@code "red"})
     */
    public String getForegroundColor() {
        return foregroundColor;
    }

    /**
     * Sets the foreground (text) color for this cell.
     *
     * @param foregroundColor the foreground color to apply; should be {@code "default"}
     *                        or one of the 16 standard terminal color names
     */
    protected void setForegroundColor(String foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    /**
     * Returns the background color of this cell.
     *
     * @return the background color string (e.g., {@code "default"}, {@code "blue"})
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color for this cell.
     *
     * @param backgroundColor the background color to apply; should be {@code "default"}
     *                        or one of the 16 standard terminal color names
     */
    protected void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Returns the style flags applied to this cell.
     *
     * @return a string representing the active style(s) (e.g., {@code "bold"}, {@code "italic"})
     */
    public String getStyle() {
        return style;
    }

    /**
     * Sets the style flags for this cell.
     *
     * @param style a string representing the desired style(s) such as {@code "bold"},
     *              {@code "italic"}, or {@code "underline"}
     */
    protected void setStyle(String style) {
        this.style = style;
    }
}