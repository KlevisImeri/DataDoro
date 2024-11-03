#!/bin/bash

# Count lines in Kotlin files
kotlin_lines=$(git ls-files '*.kt' | xargs wc -l | awk 'END {print $1}')

# Count lines in XML files
xml_lines=$(git ls-files '*.xml' | xargs wc -l | awk 'END {print $1}')

# Calculate total lines
total_lines=$((kotlin_lines + xml_lines))

# Output the results
echo "Kotlin lines: $kotlin_lines"
echo "XML lines: $xml_lines"
echo "Total lines: $total_lines"
