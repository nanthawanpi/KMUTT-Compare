db.createCollection("staff", {
  validator: {
    $jsonSchema: {
      "bsonType": "object",
      "title": "staff",
      "required": ["staffId", "staffName", "address", "email", "phone"],
      "properties": {
        "staffId": {
          "bsonType": "int"
        },
        "staffName": {
          "bsonType": "string"
        },
        "address": {
          "bsonType": "string"
        },
        "email": {
          "bsonType": "string"
        },
        "phone": {
          "bsonType": "string"
        }
      }  
    } 
  }
});
