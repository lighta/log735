#!/bin/bash

CLASSPATH=jBittorrentAPI.jar:trackerBT.jar:ext/ant.jar:ext/freemarker.jar:ext/groovy.jar:ext/jaxen-core.jar:ext/jaxen-jdom.jar:ext/jdom.jar:ext/kxml.jar:ext/saxpath.jar:ext/simple-upload-0.3.4.jar:ext/velocity.jar:extxalan.jar:ext/xerces.jar:ext/xml-apis.jar
export CLASSPATH

echo '10.196.113.23 Lighta_fedo' >> ~/.hosts
export HOSTALIASES=~/.hosts

mkdir torrent
scp lighta@Lighta_fedo:/home/lighta/Documents/ETS/LOG735/jbittorrentapi-v1.0/torrent/* ./torrent/
scp lighta@Lighta_fedo:/home/lighta/Documents/ETS/LOG735/jbittorrentapi-v1.0/*.jar ./
