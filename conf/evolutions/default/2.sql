# --- Sample dataset

# --- !Ups

insert into friend (id, fio, phone_number, social_number, email, comment) values (
  1, 'Pakhomov Alexander Sergeevich', '8-800-555-35-35', 'AlexanderPakhomov', 'Sasha@icloud.com', 'Best friend');

insert into friend (id, fio, phone_number, social_number, email, comment) values (
  2, 'Brusencev Ivan Ivanivich', '8-890-111-22-22', null, 'Brus@icloud.com', 'Good person');

insert into friend (id, fio, phone_number, social_number, email, comment) values (
  3, 'Shabanov Artemii Alexandrovich', '8-900-323-11-11', 'artemii36', 'Shopen@icloud.com', 'Blck and wt');

insert into friend (id, fio, phone_number, social_number, email, comment) values (
   4, 'Test Testovich Testov', '8-800-555-33-33', 'Testik', 'Test@gmail.com', 'Test comment');

insert into friend (id, fio, phone_number, social_number, email, comment) values (
   5, 'Testa Testovna Testova', '8-800-555-33-44', 'Testichk@', 'Test@icloud.com', 'Test comment');


# --- !Downs

delete from friend;