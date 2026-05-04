# Backlog: RSVP CLI Enhancements

## Foundation & VCS

- [x] **RSVP-000: Implement Git VCS Tracking & Atomic Commits**
- **Description:** Initialize and configure Git for project version control.
- **Details:** Establish a commit strategy emphasizing atomic, meaningful, and well-described changes. Set up `.gitignore` to exclude build/target artifacts and IDE-specific files, ensuring professional code tracking.

## Feature Enhancements

### [x] RSVP-001: Implement Play/Pause Mechanism
- **Description:** Introduce an interactive 'Pause/Resume' feature during playback.
- **Details:** When paused, the engine should hold the current chunk display, stop the countdown timer, and wait for a resume keypress (e.g., Spacebar). The terminal state (cursor hidden) should be maintained during the pause.

### RSVP-002: Dynamic Performance Feedback Loop
- **Description:** Enable automatic adjustment of WPM and pacing settings based on external performance analysis.
- **Details:** Create a secondary controller or a "profile" mode that can send signals to the running RSVP process (via local socket or signal) to adjust WPM or delay settings mid-stream. This enables external software to perform "comprehension checks" and dynamically tune the reader's pace.

### RSVP-003: Advanced Chunking & Pacing per Topic
- **Description:** Support segment-specific pacing profiles for multi-topic documents.
- **Details:** Introduce metadata tags within the input stream (e.g., `[[topic:technical,wpm:400]]`) that the engine parses in real-time to adjust settings dynamically as the reading content changes.

### RSVP-004: Flexible Input Source Handling
- **Description:** Unify input handling to support simultaneous or prioritized file and piped input.
- **Details:** Implement a more robust input manager that allows passing a file argument alongside piped input. The engine should provide a clear resolution strategy: prioritize file path if present, otherwise fallback to `stdin` (piping).

## Research & Architecture

### RSVP-005: Research Inter-Process Communication (IPC) for Real-Time Tuning
- **Description:** Investigate the feasibility of updating running RSVP processes via IPC.
- **Details:** Explore lightweight IPC mechanisms (Unix Domain Sockets or Signal handlers) that would allow an external "trainer" application to send configuration update commands (WPM, Delays) to a long-running RSVP process without needing to restart the pipeline.

### RSVP-006: Timing Jitter Analysis & Mitigation
- **Description:** Analyze and mitigate timing inaccuracies caused by `Thread.sleep()` and system I/O latency.
- **Details:** Investigate the use of higher-resolution clocks or non-blocking event loops to ensure the RSVP timing remains precise even at high WPMs (> 800) or high CPU utilization.

### RSVP-007: Closed-Loop Execution & Display Latency Monitoring
- **Description:** Implement high-precision measurement of actual frame display time vs. scheduled delay time.
- **Details:** Research if monitoring the *actual* vs *theoretical* display time is lightweight enough for real-time adjustments. Goal is a closed-loop system where the engine compensates for system I/O drift automatically.

### RSVP-008: Architectural Re-design for Large-Scale Data Handling (Streaming Engine)
- **Description:** Transition from `List<Chunk>` memory-bound processing to a true streaming iterator-based architecture.
- **Details:** Currently, the application loads the entire input (entire books) into memory via `List<Chunk>`. This is not scalable for multi-megabyte texts. Refactor the `RSVPEngine` to process an `Iterator<Chunk>` or `Stream<Chunk>` directly from the file/pipe, ensuring constant O(1) memory usage regardless of document size.

## Maintenance & Refactoring

### RSVP-009: Comprehensive Documentation of IPC Protocols
- **Description:** Define the communication protocol if external tuning is implemented.
- **Details:** Document the signals or message formats that external tools must use to communicate with the RSVP engine.
