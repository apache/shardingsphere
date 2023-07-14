#!/bin/bash

echo "skip_current_step=false" >> $GITHUB_ENV
if [[ ${changed_operations} == '["ignore"]' ]]; then
  echo "${current_operation} is ignore by ignore filter"
  echo "skip_current_step=true" >> $GITHUB_ENV
fi

if [[ ${changed_operations} == *""$current_operation""* ]]; then
  echo "${current_operation} is detected by changed filter"
  else
    echo "${current_operation} is ignore by filter"
    echo "skip_current_step=true" >> $GITHUB_ENV
fi