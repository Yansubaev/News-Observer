package com.ians.observer.presentation.helper

private const val MAX_SNIPPET_LENGTH = 200

// Shorter sentence ends are usually abbreviations like "U.S." rather than real sentences.
private const val MIN_SENTENCE_LENGTH = 40

private val sentenceEnd = Regex("""[.!?…](?=\s|$)""")

/**
 * Keeps publisher descriptions a "very short extract" (EU CDSM Art. 15):
 * the first sentence if it fits, otherwise a word-boundary cut with an ellipsis.
 */
fun shortenSnippet(text: String, maxLength: Int = MAX_SNIPPET_LENGTH): String {
    val trimmed = text.trim()

    val firstSentenceEnd = sentenceEnd.findAll(trimmed)
        .map { it.range.last + 1 }
        .firstOrNull { it >= MIN_SENTENCE_LENGTH }
    if (firstSentenceEnd != null && firstSentenceEnd <= maxLength) {
        return trimmed.substring(0, firstSentenceEnd)
    }

    if (trimmed.length <= maxLength) return trimmed

    val cut = trimmed.substring(0, maxLength)
    val wordBoundaryCut = cut.substringBeforeLast(' ').ifEmpty { cut }
    return wordBoundaryCut.trimEnd(' ', ',', ';', ':', '-', '—') + "…"
}
