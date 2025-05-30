// users
db.user.insertMany([
  {
    "userId": 1,
    "username": "johnDo",
	"name":"Jonh Doeily",
    "password": "$argon2id$v=19$m=16384,t=2,p=1$RY1vk5ECcVnJD5hIf2vyVg$RE6uKykYR5WpQI8GKtYUhKNfZSf9fG+fhn4+WS3+Piw",
    "email": "test01@example.com",
	"phone": "0812345678",
    "role": "admin",
    "createdOn": new Date("2024-05-11T12:00:00Z"), 
    "updatedOn": new Date("2024-05-31T12:00:00Z"),
	"isActive": false
  },
  {
    "userId": 2,
    "username": "janee",
	"name":"Jane Smith",
    "password": "$argon2id$v=19$m=16384,t=2,p=1$RpvMJVuDGItBmCCMAreF8g$IRHXTqnN7vXUjE2DrRINn6oy/pk4bk6fQ7+LgBdIEAE",
    "email": "test02@example.com",
	"phone": "0999999999",
    "role": "user",
    "createdOn": new Date("2024-06-15T12:00:00Z"), 
    "updatedOn": new Date("2024-07-08T12:00:00Z"),
	"isActive": false
  }
]);

//  favorites
  db.favorite.insertMany([
    {
      "favId": 1,
      "dormId": 1, 
      "userId": 1      
    },
    {
      "favId": 2,
      "dormId": 2, 
      "userId": 2      
    }
  ]);

// forms
  db.form.insertMany([
    {
      formId: 1,
      form_date: new Date("2025-01-15"),
      userId: 1,
      name: "Alice",
      email: "alice@example.com",
      phone: "0812345678",
      date_in: new Date("2025-01-20"),
      date_out: new Date("2025-01-25"),
      description: "Good dormitory",
      dormId: 1,
      status: "reserved"  // เพิ่ม status ตามโครงสร้าง
    },
    {
      formId: 2,
      form_date: new Date("2025-01-16"),
      userId: 2,
      name: "Bob",
      email: "bob@example.com",
      phone: "0898765432",
      date_in: new Date("2025-01-22"),
      date_out: new Date("2025-01-28"),
      description: "What about your new dormitory?",
      dormId: 2,
      status: "checkIn"  // เพิ่ม status ตามโครงสร้าง
    }
]);
  
// dormitorys
db.dormitory.insertOne({
  "dormId": 1,
  "dormName": "The Cozy Dorm",
  "status": "empty",
  "address": {
  "dormNumber": "111",
    "street": "Sukhumvit Road",
    "subdistrict": "Khlong Toei",
    "district": "Khlong Toei",
    "province": "Bangkok",
    "postalCode": "10110"
  },
  "roomCount": 20,
  "type": "all",
  "size": Decimal128.fromString("25.5"), // ใช้ Decimal128
  "min_price": Decimal128.fromString("3000.00"), // ใช้ Decimal128
  "max_price": Decimal128.fromString("5000.00"), // ใช้ Decimal128
  "distance": Decimal128.fromString("1.5"), // ใช้ Decimal128
  "created_at": new Date("2024-11-15T12:00:00Z"),
  "updated_at": new Date("2024-11-04T12:00:00Z"),
  "image": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ],
  "building_facility": ["CCTV","WiFi","24-hour security"],
  "room_facility": ["Air conditioning","Desk"],
"count_facilities": 5,
"rating": {
  "totalScore": Decimal128.fromString("10"),
    "count_votes": 2
  },
  "userId": 1
});

