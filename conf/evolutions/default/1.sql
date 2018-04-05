# --- First database schema

# --- !Ups

set ignorecase true;

create table friend (
  id                        bigint not null,
  fio                       varchar(255) not null,
  phone_number              varchar(255),
  social_number             varchar(255),
  email                     varchar(255),
  comment                   varchar(255),

  constraint pk_friend primary key (id))
;

create sequence friend_seq start with 1000;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists friend;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists friend_seq;

