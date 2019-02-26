create DATABASE testdatabase;
create user testuser with encrypted password 'testpass';
grant all privileges on database testdatabase to testuser;