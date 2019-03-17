#!/usr/bin/env bash

alpha4=( Alfa Bravo Charlie Delta )
alpha8=( Alfa Bravo Charlie Delta Echo Foxtrot Golf Hotel )

echo "<import_structure>"
for i in "${alpha8[@]}"; do
  echo "  <community>"
  echo "    <name>$i Community</name>"
  for j in "${alpha8[@]}"; do
    echo "    <community>"
    echo "      <name>$i $j Subcommunity</name>"
    for k in "${alpha4[@]}"; do
      echo "      <collection>"
      echo "        <name>$i $j $k Collection</name>"
      echo "      </collection>"
    done
    echo "    </community>"
  done
  echo "  </community>"
done
echo "</import_structure>"
