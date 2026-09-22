/**
 * Converts office documents to and from Markdown for language model consumption.
 *
 * <p>Supported input and output formats are {@code .docx} (via XWPF) and
 * {@code .xlsx} (via XSSF). The Markdown dialect is deliberately small and is
 * documented in {@code agents.md}: DOCX keeps headings, paragraphs, basic inline
 * bold and italic formatting, and simple tables; XLSX keeps one Markdown table
 * per worksheet and exports formula results as values.</p>
 *
 * <p>Table cells escape backslash, pipe, and newline characters as {@code \\},
 * {@code \|}, and {@code \n}. On write-back the converter infers cell types
 * using simple heuristics.</p>
 */
package agendoc.docs;
