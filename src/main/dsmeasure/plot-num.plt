set xlabel "Minutes Elapsed"
set ylabel "Quantity"
plot "data.dat" using 1:3 with lines title "Items in DSpace", "data.dat" using 1:4 with lines title "Documents in Discovery"
