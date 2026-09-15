# Agendoc — Intelligent Document Agent

## Overview

Agendoc is an intelligent agent application for working with a suite of office documents. The user can attach documents and interact with them through a chat interface powered by a Large Language Model (LLM). The application is built with Java and uses JavaFX for the graphical user interface.

## Technology Stack

- **Language:** Java 17
- **UI Framework:** JavaFX 21
- **Build System:** Gradle
- **Module System:** Java Platform Module System (JPMS)

## Project Structure

```
agendoc/
├── agents.md                  # Project description and development rules (this file)
├── build.gradle               # Gradle build script
├── settings.gradle            # Gradle settings
├── pom.xml                    # Maven POM (alternative build descriptor)
└── src/
    └── main/
        └── java/
            ├── module-info.java   # JPMS module descriptor
            └── agendoc/
                └── AgendocApp.java    # Main application entry point
```

## Core Features

1. **Document Management:** Users can attach office documents (text, spreadsheets, presentations, etc.) to the workspace.
2. **Chat Interface:** A conversational interface where users interact with an LLM to query, analyze, and manipulate the attached documents.
3. **LLM Integration:** The application connects to a Large Language Model backend to process natural language requests about the documents.

## Development Rules

### Code Style

- Opening curly braces (`{`) must be placed on a new line (Allman style).
- All comments, including Javadoc, must be written in English only.
- Use meaningful variable and method names that clearly express intent.
- Follow standard Java naming conventions: PascalCase for classes, camelCase for methods and variables.

### Build and Compilation

- The project must compile successfully before considering any task complete.
- If compilation errors occur, they must be addressed and resolved.
- Run the build after every meaningful code change to verify correctness.

### Architecture Principles

- Keep the UI layer (JavaFX) separate from business logic and data access layers.
- Use the Java module system properly; declare all required modules in `module-info.java`.
- Prefer composition over inheritance.
- Write clean, maintainable code with single-responsibility classes.

### Communication

- The assistant responds in plain text without table formatting.
- The assistant may ask clarifying questions when requirements are ambiguous.
- The assistant follows instructions from `spec.md` files when present in a directory.

## Current State

The project currently has a basic JavaFX skeleton with three UI controls:

- **TextArea** — multi-line text area for displaying document content or chat history.
- **TextField** — single-line text input for user messages.
- **ListView** — list component for displaying attached documents or conversation items.

The application window is 600×400 pixels with the title "Agendoc".