IGNORED_PATH_LIST=("./docs/document/themes" "./docs/community/content/powered-by" "./RELEASE-NOTES.md")
ignore_current_file=false
for file in $(find . -name "*.md"); do
  for ignored_path in ${IGNORED_PATH_LIST[@]}
    do
      if [[ $file =~ $ignored_path ]]; then
        ignore_current_file=true
        break
      fi
    done
  if [ "$ignore_current_file" = true ] ; then
    ignore_current_file=false
    continue
  fi
  echo "Checking $file"
  markdown-link-check -c .github/workflows/resources/linkcheck/markdown-link-check.json -q "$file"
  ignore_current_file=false
done
