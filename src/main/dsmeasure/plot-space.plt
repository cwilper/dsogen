set xlabel "Minutes Elapsed"
set ylabel "Size in MB"
plot "data.dat" using 1:5 with lines title "Solr Index", "data.dat" using 1:6 with lines title "Tomcat Heap", "data.dat" using 1:7 with lines title "CLI Heap"
