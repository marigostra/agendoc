# Agendoc — Intelligent Document Agent

## Overview

Agendoc is an intelligent agent application for working with a suite of office documents. The user can attach documents and interact with them through a chat interface powered by a Large Language Model (LLM). The application is built with Java and uses JavaFX for the graphical user interface.

## Technology Stack

- **Language:** Java 17
- **UI Framework:** JavaFX 21
- **Build System:** Gradle
- **Module System:** Java Platform Module System (JPMS)
- **Distribution Format:** Native image (GraalVM native-image)

## Distribution: Native Image

The application is distributed as a **GraalVM native-image**. This means the application is compiled ahead-of-time into a standalone native executable. Native image provides faster startup time and lower runtime memory overhead compared to running on the JVM.

### Reflection and Proxy Constraints

Because native-image performs closed-world static analysis at build time, it cannot automatically discover classes accessed via reflection, dynamic proxies, or other runtime introspection mechanisms. All such usages must be explicitly declared so that the native-image builder can include the necessary metadata.

Developers must adhere to the following rules:

- **Reflection:** Any class, method, field, or constructor accessed via `java.lang.reflect` must be explicitly registered. This is typically done via reflection configuration files (e.g., `reflect-config.json`) or annotations/hints recognized by the native-image agent and build process.
- **Dynamic Proxies:** Any use of `java.lang.reflect.Proxy` to create dynamic proxy instances requires explicit declaration of the proxied interfaces. Proxy configuration must be provided (e.g., `proxy-config.json`) so that the native-image builder generates the required proxy classes at build time.
- **Resource Bundles and Resources:** Any resources loaded via `Class.getResource()`, `ClassLoader.getResource()`, or resource bundles must be listed in the resource configuration (e.g., `resource-config.json`).
- **Serialization:** Classes used with Java serialization must be registered for reflective serialization support.
- **JNI:** Any native methods accessed via JNI must be declared in JNI configuration.

### Recommended Practice

- Run the application with the native-image tracing agent during development and testing to automatically capture reflection, proxy, resource, serialization, and JNI usage. The agent outputs configuration files that can be fed into the native-image build.
- Keep reflection usage to a minimum. Prefer direct instantiation and method calls whenever possible.
- When introducing a new library or framework, verify its native-image compatibility and include any required reachability metadata.
- The project should maintain up-to-date native-image configuration files (e.g., in `src/main/resources/META-INF/native-image/`) as part of the source tree.

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
                ├── AgendocApp.java    # Main application entry point
                └── docs/              # Office document to Markdown converters
```

## Document Exchange Format

Agendoc exchanges document content with the LLM as **Markdown**. The document type is identified only by the file name extension (`*.docx` or `*.xlsx`); the model receives the file name together with its content, so no separate type marker is required inside the Markdown body.

### DOCX

A DOCX document is converted to the following Markdown subset:

- Headings: one to six leading `#` characters.
- Regular paragraphs.
- Inline formatting: `**bold**`, `*italic*`, and `***bold italic***`.
- Simple tables without merged cells. The first table row is treated as the header row.

The converters handle only basic formatting. Styling, images, footnotes, headers, footers, and complex layouts are intentionally ignored: the goal is to transfer the actual data, and the user can apply visual polish afterward.

### XLSX

An XLSX workbook is converted to a sequence of Markdown sections:

- Each worksheet starts with a top-level heading: `# SheetName`.
- The used rectangular range of the sheet follows as a Markdown table.
- The first row of the used range is treated as the header row.
- Cell values are exported as their evaluated values. Formulas are converted to values, and the formula definition itself is lost.
- Empty cells are preserved as empty table cells so that the rectangular layout is not lost.

When writing Markdown back to XLSX, the heading becomes the worksheet name, and the tables are written as worksheet rows.

### Table Cell Escaping

Inside table cells the following escape sequences are used:

- `\\` — backslash.
- `\|` — pipe character.
- `\n` — newline.

Carriage return characters are removed during conversion.

### Cell Type Inference on Write

When writing XLSX, cell values are inferred from the text:

- `true` and `false` (case-insensitive) become boolean cells.
- Integer and floating-point values become numeric cells, unless the value starts with a leading zero and contains only digits, in which case it is kept as text.
- Empty values leave the cell blank.
- All other values are written as text cells.

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

### Native Image Readiness

- Every code change must consider native-image compatibility. Avoid introducing unregistered reflection, dynamic proxies, or resource loading without corresponding configuration entries.
- When reflection or proxies are unavoidable, document the usage and ensure the corresponding native-image configuration files are updated.
- The project must remain buildable as a native image at all times.

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
