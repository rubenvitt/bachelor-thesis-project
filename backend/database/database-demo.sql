insert into appUser (password, name, mail)
values ('123', 'Peter', 'peter@peter.de'),
       ('222', 'Hans', 'hans@hans.de');

insert into credential (credential, users_id)
values ('google-0001', 1),
       ('outlook-0001', 1),
       ('google-0002', 2);