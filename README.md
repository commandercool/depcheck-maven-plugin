# Maven plugin to generate and check checksums of maven dependencies

The project was inspired by this [post on stackoverflow](http://stackoverflow.com/questions/15889370/lock-dependencies-by-secure-checksum-in-maven).

The plugin itself is very simple and has two goals:

* hash - hashes all dependency artifacts of the project and puts md5 file in the project parent directory; 
* check - checks artifact's hashes against this files.
