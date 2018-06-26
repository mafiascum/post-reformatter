#! /bin/sh

echo "Dir: `pwd`"

java -Xms1024m -Xmx2048m -cp /usr/src/ms-post-reformatter/ -cp '/usr/src/ms-post-reformatter/lib/*' net.mafiascum.reformatter.driver.MainDriver $*
