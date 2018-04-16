Library Application
=======================

Simple example how to use Scala with MongoDB.

### Install MongoDB on Linux:

* download the latest release through the shell, issue the following:

        curl -O https://fastdl.mongodb.org/linux/mongodb-linux-x86_64-3.6.3.tgz
        
* extract the files from the downloaded archive:

        tar -zxvf mongodb-linux-x86_64-3.6.3.tgz
        
* copy the extracted archive to the target directory    

        mkdir -p mongodb
        cp -R -n mongodb-linux-x86_64-3.6.3/ mongodb
        
* modify your PATH

        export PATH=<mongodb-install-directory>/bin:$PATH
        
* create the data directory and set permissions for the data directory

        mkdir -p /data/db
        chmod -R <readwritesettings> /data/db
        
* run MongoDB

        mongod

### Relation model:

![alt text](public/images/model.jpg)
