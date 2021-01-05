#!/bin/bash
git clone https://github.com/FIWARE/tutorials.Getting-Started.git
cd tutorials.Getting-Started
sudo docker-compose -p fiware up -d
cd -
