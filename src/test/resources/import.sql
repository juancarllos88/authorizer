insert into accounts(id, name,inserted_at,updated_at)  values ('554e590b-a4bc-4859-b245-cbb4701fdbbd', 'Edu', now(), now());

insert into balances(id, type, amount, account_id,inserted_at,updated_at) values (gen_random_uuid (),'FOOD',10000,'554e590b-a4bc-4859-b245-cbb4701fdbbd', now(), now());
insert into balances(id, type, amount, account_id,inserted_at,updated_at) values (gen_random_uuid (),'MEAL',10000,'554e590b-a4bc-4859-b245-cbb4701fdbbd', now(), now());
insert into balances(id, type, amount, account_id,inserted_at,updated_at) values (gen_random_uuid (),'CASH',10000,'554e590b-a4bc-4859-b245-cbb4701fdbbd', now(), now());

insert into merchants(id, name, balance_type)  values (gen_random_uuid (),'PADARIA DO ZE               SAO PAULO BR','FOOD');

insert into merchants_category_code(id, code, balance_type) values (gen_random_uuid (),'5811','MEAL');
