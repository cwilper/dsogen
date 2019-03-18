#!/usr/bin/env bash

sizes=( Tiny Small Medium Big )
colors=( Black Blue Brown Green Orange Pink Red Yellow )
things=( Bird Crayon Elephant Fish Grasshopper Light Lobster Sky )

echo "<import_structure>"
for i in "${sizes[@]}"; do
  echo "  <community>"
  echo "    <name>$i</name>"
  for j in "${colors[@]}"; do
    echo "    <community>"
    echo "      <name>$i $j</name>"
    for k in "${things[@]}"; do
      echo "      <collection>"
      echo "        <name>$i $j $k</name>"
      echo "      </collection>"
    done
    echo "    </community>"
  done
  echo "  </community>"
done
echo "</import_structure>"
