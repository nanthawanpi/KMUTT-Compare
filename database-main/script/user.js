db.createCollection("user", {
  validator: {
    $jsonSchema: {
      "bsonType": "object",
      "title": "user",
      "required": ["userId", "username", "password", "email", "role", "createdOn", "updatedOn"],
      "properties": {
        "userId": {
          "bsonType": "int"
        },
        "username": {
          "bsonType": "string"
        },
        "password": {
          "bsonType": "string"
        },
        "email": {
          "bsonType": "string"
        },
        "role": {
          "enum": ['admin','user','guest']
        },
        "createdOn": {
          "bsonType": "timestamp"
        },
        "updatedOn": {
          "bsonType": "timestamp"
        }
      }  
    } 
  }
});
