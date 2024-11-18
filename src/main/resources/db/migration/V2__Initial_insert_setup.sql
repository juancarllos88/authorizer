insert into accounts(id, name,inserted_at,updated_at)  values ('554e590b-a4bc-4859-b245-cbb4701fdbbd', 'Edu', now(), now());
insert into accounts(id, name,inserted_at,updated_at)  values ('fcd31a0b-3bfa-4996-9f74-74d51e98ed10', 'Monica', now(), now());

insert into balances(id, type, amount, account_id,inserted_at,updated_at) values (gen_random_uuid (),'FOOD',10000,'554e590b-a4bc-4859-b245-cbb4701fdbbd', now(), now());
insert into balances(id, type, amount, account_id,inserted_at,updated_at) values (gen_random_uuid (),'MEAL',10000,'554e590b-a4bc-4859-b245-cbb4701fdbbd', now(), now());
insert into balances(id, type, amount, account_id,inserted_at,updated_at) values (gen_random_uuid (),'CASH',10000,'554e590b-a4bc-4859-b245-cbb4701fdbbd', now(), now());
insert into balances(id, type, amount, account_id,inserted_at,updated_at) values (gen_random_uuid (),'FOOD',10000,'fcd31a0b-3bfa-4996-9f74-74d51e98ed10', now(), now());
insert into balances(id, type, amount, account_id,inserted_at,updated_at) values (gen_random_uuid (),'MEAL',10000,'fcd31a0b-3bfa-4996-9f74-74d51e98ed10', now(), now());

insert into merchants(id, name, balance_type)  values (gen_random_uuid (),'UBER TRIP                   SAO PAULO BR ','CASH');
insert into merchants(id, name, balance_type)  values (gen_random_uuid (),'UBER EATS                   SAO PAULO BR','MEAL');
insert into merchants(id, name, balance_type)  values (gen_random_uuid (),'PAG*JoseDaSilva          RIO DE JANEI BR','FOOD');
insert into merchants(id, name, balance_type)  values (gen_random_uuid (),'PICPAY*BILHETEUNICO           GOIANIA BR','CASH');
insert into merchants(id, name, balance_type)  values (gen_random_uuid (),'PADARIA DO ZE               SAO PAULO BR','FOOD');

insert into merchants_category_code(id, code, balance_type) values (gen_random_uuid (),'5411','FOOD');
insert into merchants_category_code(id, code, balance_type) values (gen_random_uuid (),'5412','FOOD');
insert into merchants_category_code(id, code, balance_type) values (gen_random_uuid (),'5811','MEAL');
insert into merchants_category_code(id, code, balance_type) values (gen_random_uuid (),'5812','MEAL');