#!/usr/bin/env bash
# PreToolUse guard: deny any tool call that READS or TARGETS the real,
# gitignored deployment secrets file. The committed .example template is
# intentionally left readable.
#
# Scans path/command-bearing fields of tool_input (file_path, path, the Bash
# command, the Grep pattern/path, ...) plus the tool name, so there is no
# `cat`-the-file or grep-the-file bypass. Free-text payload fields
# (content/new_string/old_string) are EXCLUDED so Claude can still write/edit
# files that merely MENTION the filename (docs, this guard, the redaction hook)
# without being blocked.
input=$(cat)

# Flatten tool name + path/command strings; drop free-text edit/write payloads.
haystack=$(printf '%s' "$input" | jq -r '[(.tool_name // ""), (.tool_input // {} | del(.content, .new_string, .old_string) | .. | strings)] | join("\n")' 2>/dev/null)

# Match the basename but stop short of the .example/.sample template
# (the char right after ".env" must be a path/quote/whitespace boundary, not ".").
if printf '%s' "$haystack" | grep -Eq 'release-manager\.yml([^.A-Za-z0-9]|$)'; then
  cat <<'JSON'
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "deny",
    "permissionDecisionReason": "Access to release-manager.yml is blocked by policy — it holds real deployment secrets. Use release-manager-sample.yml for the expected structure."
  }
}
JSON
fi
exit 0
