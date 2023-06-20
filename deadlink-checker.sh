for file in $(find . -path "./docs/document/themes" -prune -o -name "*.md"); do
    if [ -d "$file" ]; then
      continue
    fi
  markdown-link-check -c .github/workflows/resources/linkcheck/markdown-link-check.json -q "$file"
done
