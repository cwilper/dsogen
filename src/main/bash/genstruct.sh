#!/usr/bin/env bash

sizes=( Tiny Small Medium Big )
colors=( Black Blue Brown Green Orange Pink Red Yellow )
things=( Bird Crayon Elephant Fish Grasshopper Light Lobster Sky )

echo "<import_structure>"
for i in "${colors[@]}"; do
  echo "  <community>"
  echo "    <name>$i</name>"
  for j in "${things[@]}"; do
    echo "    <community>"
    echo "      <name>$i $j</name>"
    for k in "${sizes[@]}"; do
      echo "      <collection>"
      echo "        <name>$k $i $j</name>"
      echo "      </collection>"
    done
    echo "    </community>"
  done
  echo "  </community>"
done
echo "</import_structure>"
