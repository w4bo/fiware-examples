#!/bin/bash
sudo docker stop fiware-orion
sudo docker rm fiware-orion
sudo docker stop mongo-db
sudo docker rm mongo-db
cd tutorials.Getting-Started
sudo docker-compose -p fiware down
cd -
sudo docker network rm fiware_default
