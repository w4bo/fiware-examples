#!/bin/bash
set -e
sudo docker pull mongo:4.2
sudo docker pull fiware/orion
sudo docker network create fiware_default
sudo docker run -d --name=mongo-db --network=fiware_default --expose=27017 mongo:4.2 --bind_ip_all --smallfiles
sudo docker run -d --name fiware-orion -h orion --network=fiware_default -p 1026:1026 fiware/orion -dbhost mongo-db
sudo docker ps
