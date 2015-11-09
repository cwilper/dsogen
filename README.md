# dsogen

Generates DSpace objects, for testing.

## Installation

**Prerequisites:**

* JDK 8
* Maven 3

**Cloning and Building:**

    git clone https://github.com/cwilper/dsogen.git
    cd dsogen
    mvn package

**Execution:**

The easy way to start using dsogen is just to add ``/path/to/dsogen/src/main/bash`` to your ``PATH``, which will put the ``dsogen`` command available to you.

You can also just run ``java -jar target/dsogen.jar`` and not worry about putting it in your path.

## Usage

    dsogen -out out-dir -num num-items [options..]

    Options:

    -bin <arg>        Include the given number of binary bitstreams for each item,
                      in the ORIGINAL bundle. If unspecified, none will be added.
    -maxbytes <arg>   Maximum bytes for .bin bitstreams. Default is 1.
    -maxpages <arg>   Maximum pages of text for .txt and .pdf bitstreams.
                      Default is 1.
    -minbytes <arg>   Minimum bytes for .bin bitstreams. Default is 1.
    -minpages <arg>   Minimum pages of text for .txt and .pdf bitstreams.
                      Default is 1.
    -num <arg>        Number of items to generate. Default is 1.
    -out <arg>        Output directory. It will be created if it doesn't exist yet.
    -pdf              Include a PDF bitstream for each item, in the ORIGINAL bundle.
                      If specified, this will be the primary bitstream.
    -txt              Include a plaintext bitstream for each item, in the TEXT bundle.
                      If specified, this will be the primary bitstream if -pdf is not
                      specified.

    At least one of -pdf, -txt, or -bin must be specified, in any combination.

## Examples

Generate 50 objects, each containing a 10 to 15-page PDF as the primary
bitstream in the ORIGINAL bundle, with a corresponding plaintext file in
the TEXT bundle:

    dsogen -out out -num 50 -minpages 10 -maxpages 15 -pdf -txt

Generate one million objects, each with a single one-byte bitstream:

    dsogen -out out -num 1000000 -bin 1
