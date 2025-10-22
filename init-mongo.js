db = db.getSiblingDB('user_database');
db.createUser({
  user: "admin",
  pwd: "admin123",
  roles: [
    { role: "readWrite", db: "user_database" },
    { role: "readWrite", db: "admin" }
  ]
});
