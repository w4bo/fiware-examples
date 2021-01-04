curl -X GET 'http://localhost:3000/health/random'
echo ''
curl -X GET 'http://localhost:3000/health/weather'
echo ''
curl -iX POST \
  'http://localhost:3000/static/temperature/op/query' \
  -H 'Content-Type: application/json' \
  -d '{
    "entities": [
        {
            "type": "Store",
            "isPattern": "false",
            "id": "urn:ngsi-ld:Store:001"
        }
    ],
    "attrs": [
        "temperature"
    ]
}'
echo ''
curl -iX POST \
  'http://localhost:3000/random/weatherConditions/op/query' \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/json' \
  -H 'Postman-Token: 2ae9e6d6-802b-4a62-a561-5c7739489fb3' \
  -d '{
    "entities": [
        {
            "type": "Store",
            "isPattern": "false",
            "id": "urn:ngsi-ld:Store:001"
        }
    ],
    "attrs": [
        "temperature",
        "relativeHumidity"
    ]
}'
echo ''
