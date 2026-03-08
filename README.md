# Integration-of-an-external-terminal-emulator-into-the-IntelliJ-Terminal

# terminal-buffer

A Java library for managing a terminal screen buffer with scrollback history. Designed as a foundation for terminal emulators, TUI frameworks, or any application that needs to model a grid of styled characters.

---

## Features

- Fixed-size screen buffer with configurable width, height, and scrollback limit
- Per-cell color (foreground + background) and style attributes (bold, italic, underline)
- Line-wrap tracking so logical lines spanning multiple rows can be reconstructed
- Cursor movement with automatic screen-edge clamping
- Scrollback history with configurable maximum size
- Insert mode with overflow cascading across wrapped lines

---

## Project Structure

```
src
├── main/java/terminal
│   ├── buffer
│   │   ├── TerminalBuffer.java   # Core buffer - the main API entry point
│   │   ├── TerminalLine.java     # A single row of character cells
│   │   └── CharacterCell.java   # A single cell: character + colors + styles
│   ├── cursor
│   │   └── CursorPosition.java  # Cursor position with bounds clamping
│   └── style
│       ├── Color.java            # Foreground/background color enum (16 ANSI colors)
│       └── Style.java            # Text style enum (BOLD, ITALIC, UNDERLINE)
└── test/java/terminal
    ├── buffer
    │   ├── TerminalBufferTests.java
    │   ├── TerminalLineTests.java
    │   └── CharacterCellTests.java
    └── cursor
        └── CursorPositionTests.java
```

---

## Getting Started

### Requirements

- Java 11+
- Maven 3.6+

### Build

```bash
mvn clean install
```

### Run Tests

```bash
mvn test
```

---

## Usage

### Creating a Buffer

```java
// 80 columns wide, 24 rows tall, 1000 lines of scrollback
TerminalBuffer buffer = new TerminalBuffer(80, 24, 1000);
```

### Writing Text

```java
buffer.writeText('H');
buffer.writeText('i');

// Read it back
String line = buffer.getScreenLineAsString(0); // "Hi"
```

### Applying Colors and Styles

```java
import java.util.Set;

buffer.setAttributes(Color.GREEN, Color.BLACK, Set.of(Style.BOLD));
buffer.writeText('$');
buffer.writeText(' ');

// Cell attributes are stored per-character
CharacterCell cell = buffer.getCellAt(0, 0);
cell.getForegroundColor(); // GREEN
cell.getStyles();          // [BOLD]
```

### Moving the Cursor

```java
CursorPosition cursor = buffer.getCursor();

cursor.moveRight(5);
cursor.moveDown(2);
cursor.setPosition(0, 0); // top-left
```

### Reading Screen Content

```java
// Single line as a string
buffer.getScreenLineAsString(3);

// Entire visible screen (wrapped lines are joined without a newline)
buffer.getScreenAsString();

// Full buffer including scrollback
buffer.getFullContentAsString();
```

### Scrollback

```java
// Read a line from the scrollback history (above the visible screen)
// Row 0 = oldest line
buffer.getScrollbackLineAsString(0);
buffer.getScrollbackCellAt(0, 5);
```

### Clearing the Screen

```java
buffer.clearEntireScreen();        // Clears visible screen, preserves scrollback
buffer.clearScreenAndScrollback(); // Full reset
```

---

## API Reference

### `TerminalBuffer`

| Method | Description |
|---|---|
| `writeText(char)` | Write a character at the cursor and advance it |
| `insertText(char)` | Insert a character at the cursor, shifting content right |
| `fillLine(char)` | Fill the current row with a character |
| `setAttributes(fg, bg, styles)` | Set colors and styles for subsequent writes |
| `getCellAt(row, col)` | Get a cell from the visible screen |
| `getScrollbackCellAt(row, col)` | Get a cell from scrollback history |
| `getScreenAsString()` | Render the visible screen as a string |
| `getFullContentAsString()` | Render the entire buffer including scrollback |
| `clearEntireScreen()` | Clear the visible screen |
| `clearScreenAndScrollback()` | Full reset of all content and cursor |
| `getCursor()` | Return the current `CursorPosition` |
| `setCursor(row, col)` | Move the cursor to an absolute position |

### `CursorPosition`

| Method | Description |
|---|---|
| `moveLeft/Right/Up/Down()` | Move one step, clamped to screen bounds |
| `moveLeft/Right/Up/Down(n)` | Move n steps, clamped to screen bounds |
| `setPosition(row, col)` | Jump to an absolute position |
| `getRow() / getCol()` | Read current position |

### `Color` (enum)

16 standard ANSI colors: `BLACK`, `RED`, `GREEN`, `YELLOW`, `BLUE`, `MAGENTA`, `CYAN`, `WHITE`, and their `BRIGHT_*` variants.

### `Style` (enum)

`BOLD`, `ITALIC`, `UNDERLINE`

---

## License

[MIT](LICENSE)
