#!/bin/bash

wordnet_filename=wn3.1.dict.tar.gz
wordnet_db_url=http://wordnetcode.princeton.edu/$wordnet_filename

die() {
  echo "Error: $1"
  exit 1
}

extract_words() {
  egrep -o "^[0-9]{8}\s[0-9]{2}\s[a-z]\s[0-9]{2}\s[a-zA-Z_]*\s" dict/$1 | cut -d ' ' -f 5 | sed 's/_/ /g' | sort -u >> words.txt
}

cd $(dirname $0)/../resources/com/github/cwilper/dsogen

if [[ ! -f "words.txt" ]]; then
  echo "Populating word list from Wordnet"
  if [[ ! -f $wordnet_filename ]]; then
    echo "Downloading $wordnet_db_url"
    wget "$wordnet_db_url" > /dev/null 2>&1 || die "Error downloading"
  fi
  rm -rf dict > /dev/null 2>&1
  echo "Extracting $wordnet_filename"
  tar -xf $wordnet_filename > /dev/null 2>&1 || die "Error extracting"
  extract_words data.adj
  extract_words data.adv
  extract_words data.noun
  extract_words data.verb
  rm -rf dict > /dev/null 2>&1
  rm $wordnet_filename > /dev/null 2>&1
  echo "Done"
else
  echo "Word list is already populated."
fi
