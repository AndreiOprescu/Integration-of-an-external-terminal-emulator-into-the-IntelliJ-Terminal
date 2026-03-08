package terminal;

import java.util.HashSet;
import java.util.Set;

/**
 * A single cell in the terminal grid, holding a character and its visual attributes.
 *
 * <p>Every position on screen maps to one of these. A null character means the cell
 * hasn't been written to yet - callers should treat it as an empty space. Colors and
 * styles are stored per-cell so each character can be rendered independently.</p>
 *
 * <p>The mutation setters are package-private to keep writes funnelled through
 * {@link TerminalBuffer}, which is the only thing that should be changing cell state.</p>
 */
public class CharacterCell {

    /** The character at this position, or null if nothing has been written here. */
    Character character;

    /** Text (foreground) color. */
    Color foregroundColor;

    /** Background color shown behind the character. */
    Color backgroundColor;

    /** Active style flags for this cell - bold, italic, underline, etc. */
    Set<Style> styles;

    /**
     * Full constructor - sets every attribute explicitly.
     * The styles set is defensively copied so later changes to the caller's set
     * don't silently affect this cell.
     *
     * @param character       the character to display, or null for an empty cell
     * @param foregroundColor text color
     * @param backgroundColor background color
     * @param styles          style flags to apply
     */
    public CharacterCell(Character character, Color foregroundColor, Color backgroundColor, Set<Style> styles) {
        this.character = character;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
        this.styles = new HashSet<>(styles);
    }

    /**
     * Convenience constructor for when you don't need any styles.
     *
     * @param character       the character to display
     * @param foregroundColor text color
     * @param backgroundColor background color
     */
    public CharacterCell(Character character, Color foregroundColor, Color backgroundColor) {
        this(character, foregroundColor, backgroundColor, new HashSet<>());
    }

    /**
     * Minimal constructor - uses white text on black with no styles.
     * Handy for plain text where you don't care about colors.
     *
     * @param character the character to display
     */
    public CharacterCell(Character character) {
        this(character, Color.WHITE, Color.BLACK, new HashSet<>());
    }

    /** Returns the character stored at this position, or null if the cell is empty. */
    public Character getCharacter() {
        return character;
    }

    /**
     * Sets the character for this cell. Pass null to mark it as empty.
     *
     * @param character the character to store
     */
    protected void setCharacter(Character character) {
        this.character = character;
    }

    /** Returns the foreground (text) color. */
    public Color getForegroundColor() {
        return foregroundColor;
    }

    /**
     * Sets the foreground color.
     *
     * @param foregroundColor the color to apply to the text
     */
    protected void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    /** Returns the background color. */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color.
     *
     * @param backgroundColor the color to render behind the character
     */
    protected void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Returns the live set of styles applied to this cell.
     * Note that this is not a copy - mutating the returned set will affect the cell directly.
     */
    public Set<Style> getStyles() {
        return styles;
    }

    /**
     * Adds a style flag to this cell. Has no effect if the style is already present.
     *
     * @param style the style to add
     */
    protected void addStyle(Style style) {
        this.styles.add(style);
    }

    /**
     * Removes a style flag from this cell.
     *
     * @param style the style to remove
     * @return true if the style was present and got removed, false if it wasn't there
     */
    protected boolean removeStyle(Style style) {
        if (styles.contains(style)) {
            styles.remove(style);
            return true;
        }
        return false;
    }

    /**
     * Replaces the entire styles set. Unlike the constructor, this does not make
     * a defensive copy - the cell will hold a direct reference to the set you pass in.
     *
     * @param styles the new set of styles
     */
    public void setStyles(Set<Style> styles) {
        this.styles = styles;
    }

    /**
     * Returns the character as a string. Will throw a NullPointerException if the
     * character is null, so check that before calling this on potentially empty cells.
     */
    @Override
    public String toString() {
        return this.character.toString();
    }
}