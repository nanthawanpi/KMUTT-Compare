db.createCollection("dormitory", {
  validator: {
    $jsonSchema: {
      "bsonType": "object",
      "title": "dormitory",
      "required": ["dormId", "name", "status", "address", "roomCount", "type", "size", "min_price", "max_price", "distance", "created_at", "updated_at", "building_facility", "room_facility", "staff"],
      "properties": {
        "dormId": {
          "bsonType": "int"
        },
        "name": {
          "bsonType": "string"
        },
        "status": {
          "enum": ['empty','full']
        },
        "address": {
          "bsonType": "object",
          "title": "object",
          "required": ["street", "subdistrict", "district", "province", "postalCode"],
          "properties": {
            "street": {
              "bsonType": "string"
            },
            "subdistrict": {
              "bsonType": "string"
            },
            "district": {
              "bsonType": "string"
            },
            "province": {
              "bsonType": "string"
            },
            "postalCode": {
              "bsonType": "string"
            }
          }  
        },
        "roomCount": {
          "bsonType": "int"
        },
        "type": {
          "enum": ['all','f','m']
        },
        "size": {
          "bsonType": "decimal"
        },
        "min_price": {
          "bsonType": "decimal"
        },
        "max_price": {
          "bsonType": "decimal"
        },
        "distance": {
          "bsonType": "decimal"
        },
        "created_at": {
          "bsonType": "timestamp"
        },
        "updated_at": {
          "bsonType": "timestamp"
        },
        "image": {
          "bsonType": "array",
          "items": {
            "bsonType": "string"
          }
        },
        "building_facility": {
          "bsonType": "array",
          "items": {
            "bsonType": "string"
          }
        },
        "room_facility": {
          "bsonType": "array",
          "items": {
            "bsonType": "string"
          }
        },
        "staff": {
          "bsonType": "int"
        }
      }  
    } 
  },
  "validationLevel":"strict",
  "validationAction":"error"
});
