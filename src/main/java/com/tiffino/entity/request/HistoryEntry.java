package com.tiffino.entity.request;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HistoryEntry {
    private final String prompt;
    private final String response;

    @Override
    public String toString() {
        String safePrompt = sanitize(prompt);
        String safeResponse = sanitize(response);
        return String.format(
                "`history_entry`:\n    `prompt`: \"%s\"\n\n    `response`: \"%s\"\n-----------------\n",
                safePrompt, safeResponse
        );
    }

    private String sanitize(String s) {
        if (s == null) return "";
        return s.replace("`", "'").replaceAll("\\p{Cntrl}", " ");
    }
}
