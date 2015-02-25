# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table guest (
  id                        bigint not null,
  name                      varchar(255),
  household                 varchar(255),
  attending_wedding         boolean,
  attending_rehearsal       boolean,
  last_update_date          timestamp,
  constraint pk_guest primary key (id))
;

create sequence guest_seq;




# --- !Downs

drop table if exists guest cascade;

drop sequence if exists guest_seq;

