# RSVP CLI

A terminal-based Rapid Serial Visual Presentation (RSVP) reader designed for speed reading in the command line.

## Core Philosophy
RSVP improves reading speed by presenting text chunks in a single, central location on your screen. This reduces the need for eye movement, allowing you to maintain focus and increase your reading throughput.

## Features
- **Dynamic Chunking:** Accepts standard input, making it compatible with any external chunking tool or LLM pipeline.
- **Natural Pacing:** Punctuation-aware delay logic (commas, periods, etc.) to mimic natural reading pauses.
- **Interactive Controls:** Adjust WPM or navigate chunks in real-time without needing to press Enter.
- **State Aware:** Remembers your preferred reading speed across sessions.
- **Configuration:** Customizable punctuation pause multipliers via XDG-standard configuration files.

## Installation & Usage

### Building
```bash
mvn clean package
```

### Quick Start
Pipe text into the application:
```bash
echo "This is a speed reading test." | java -jar target/chunk-learn-1.0-SNAPSHOT.jar
```

### Command Line Options
| Option | Description |
| :--- | :--- |
| `-h, --help` | Display help menu |
| `--init` | Generate default configuration file |
| `-wpm, --words-per-minute=WPM` | Set base words per minute (Default: 300) |
| `-sd, --stop-delay=MS` | Set additive stop punctuation delay in ms (Default: 30) |
| `-pd, --pause-delay=MS` | Set additive pause punctuation delay in ms (Default: 10) |
| `-sm, --stop-multiplier=PERCENT` | Set stop punctuation increase (percentage of chunk time) |
| `-pm, --pause-multiplier=PERCENT` | Set pause punctuation increase (percentage of chunk time) |

### Precedence
When both configuration methods are used:
1. **Command Line Flags** take highest priority.
2. **Configuration File** (`~/.config/chunk-rsvp/config.properties`) takes secondary priority.
3. **Hardcoded Defaults** are used if no other configuration exists.

**Note:** Millisecond delays (`--stop-delay`, `--pause-delay`) take priority over percentage multipliers if both are configured for the same punctuation type.


## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss the proposed updates.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
