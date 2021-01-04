#!/bin/bash
# get version
curl -X GET 'http://localhost:1026/version'
#get version
sudo docker run --network fiware_default --rm appropriate/curl -s -X GET 'http://orion:1026/version'
# add entity
curl -iX POST \
  'http://localhost:1026/v2/entities' \
  -H 'Content-Type: application/json' \
  -d '
{
    "id": "urn:ngsi-ld:Store:001",
    "type": "Store",
    "address": {
        "type": "PostalAddress",
        "value": {
            "streetAddress": "Bornholmer Straße 65",
            "addressRegion": "Berlin",
            "addressLocality": "Prenzlauer Berg",
            "postalCode": "10439"
        },
        "metadata": {
            "verified": {
                "value": true,
                "type": "Boolean"
            }
        }
    },
    "location": {
        "type": "geo:json",
        "value": {
             "type": "Point",
             "coordinates": [13.3986, 52.5547]
        }
    },
    "name": {
        "type": "Text",
        "value": "Bösebrücke Einkauf"
    }
}'
echo ''

# query the entity
curl -G -X GET \
	   'http://localhost:1026/v2/entities/urn:ngsi-ld:Store:001' \
	       -d 'options=keyValues'
echo ''

# query the entity by type
curl -G -X GET \
	    'http://localhost:1026/v2/entities' \
	        -d 'type=Store' \
	        -d 'options=keyValues'
echo ''
