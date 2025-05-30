db.createCollection("favorite", {
  validator: {
    $jsonSchema: {
      "bsonType": "object",
      "title": "favorite",
      "required": ["favId", "dormitory", "user"],
      "properties": {
        "favId": {
          "bsonType": "int"
        },
        "dormitory": {
          "bsonType": "int"
        },
        "user": {
          "bsonType": "int"
        }
      }  
    } 
  }
});
