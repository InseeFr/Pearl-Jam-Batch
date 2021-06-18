INSERT INTO public.address (dtype, l1, l2, l3, l4, l5, l6, l7, geographical_location_id) VALUES ('InseeAddress', 'Ted Farmer' ,'','','1 rue de la gare' ,'','29270 Carhaix' ,'France', '29024');
INSERT INTO public.address (dtype, l1, l2, l3, l4, l5, l6, l7, geographical_location_id) VALUES ('InseeAddress', 'Cecilia Ortega' ,'','','2 place de la mairie' ,'','90000 Belfort' ,'France', '90010');
INSERT INTO public.address (dtype, l1, l2, l3, l4, l5, l6, l7, geographical_location_id) VALUES ('InseeAddress', 'Claude Watkins' ,'','','3 avenue de la République' ,'','32230 Marciac' ,'France', '32233');
INSERT INTO public.address (dtype, l1, l2, l3, l4, l5, l6, l7, geographical_location_id) VALUES ('InseeAddress', 'Veronica Gill' ,'','','4 chemin du ruisseau' ,'','44190 Clisson' ,'France', '44043');
INSERT INTO public.address (dtype, l1, l2, l3, l4, l5, l6, l7, geographical_location_id) VALUES ('InseeAddress', 'Christine Aguilar' ,'','','5 rue de l''école' ,'','59620 Aulnoye-Aimeries' ,'France', '59033');
INSERT INTO public.address (dtype, l1, l2, l3, l4, l5, l6, l7, geographical_location_id) VALUES ('InseeAddress', 'Louise Walker' ,'','','6 impasse du lac' ,'','38200 Vienne' ,'France', '38544');
INSERT INTO public.address (dtype, l1, l2, l3, l4, l5, l6, l7, geographical_location_id) VALUES ('InseeAddress', 'Anthony Bennett' ,'','','7 avenue de la Liberté' ,'','62000 Arras' ,'France', '62041');
INSERT INTO public.address (dtype, l1, l2, l3, l4, l5, l6, l7, geographical_location_id) VALUES ('InseeAddress', 'Christopher Lewis' ,'','','8 route du moulin' ,'','35000 Rennes' ,'France', '35238');

INSERT INTO public.campaign (id, collection_end_date, collection_start_date, label) VALUES ('SIMPSONS2020X00', (SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '2 DAY'))*1000), (SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '1 DAY'))*1000), 'Survey on the Simpsons tv show 2020');
INSERT INTO public.campaign (id, collection_end_date, collection_start_date, label) VALUES ('VQS2021X00', (SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '2 DAY'))*1000), (SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '1 DAY'))*1000), 'Everyday life and health survey 2021');

INSERT INTO public.organization_unit (id, label, type, organization_unit_parent_id) VALUES ('OU-NATIONAL', 'National organizational unit', 'NATIONAL', null);
INSERT INTO public.organization_unit (id, label, type, organization_unit_parent_id) VALUES ('OU-NORTH', 'North region organizational unit', 'LOCAL', 'OU-NATIONAL');
INSERT INTO public.organization_unit (id, label, type, organization_unit_parent_id) VALUES ('OU-SOUTH', 'South region organizational unit', 'LOCAL', 'OU-NATIONAL');

INSERT INTO public.interviewer (id, email, first_name, last_name, phone_number, organization_unit_id) VALUES ('INTW1', 'margie.lucas@ou.com', 'Margie', 'Lucas', '+3391231231230', 'OU-NORTH');
INSERT INTO public.interviewer (id, email, first_name, last_name, phone_number, organization_unit_id) VALUES ('INTW2', 'carlton.campbell@ou.com', 'Carlton', 'Campbell', '+3391231231231', 'OU-NORTH');
INSERT INTO public.interviewer (id, email, first_name, last_name, phone_number, organization_unit_id) VALUES ('INTW3', 'gerald.edwards@ou.com', 'Gerald', 'Edwards', '+3391231231231', 'OU-NORTH');
INSERT INTO public.interviewer (id, email, first_name, last_name, phone_number, organization_unit_id) VALUES ('INTW4', 'melody.grant@ou.com', 'Melody', 'Grant', '+3391231231231', 'OU-SOUTH');

INSERT INTO public.sample_identifier (dtype, autre, bs, ec, le, nograp, noi, nole, nolog, numfa, rges, ssech) VALUES ('InseeSampleIdentifier', '11', 11, '1', 11, '11', 11, 11, 11, 11, 11, 1);
INSERT INTO public.sample_identifier (dtype, autre, bs, ec, le, nograp, noi, nole, nolog, numfa, rges, ssech) VALUES ('InseeSampleIdentifier', '12', 12, '1', 12, '12', 12, 12, 12, 12, 12, 1);
INSERT INTO public.sample_identifier (dtype, autre, bs, ec, le, nograp, noi, nole, nolog, numfa, rges, ssech) VALUES ('InseeSampleIdentifier', '13', 13, '1', 13, '13', 13, 13, 13, 13, 13, 2);
INSERT INTO public.sample_identifier (dtype, autre, bs, ec, le, nograp, noi, nole, nolog, numfa, rges, ssech) VALUES ('InseeSampleIdentifier', '14', 14, '1', 14, '14', 14, 14, 14, 14, 14, 3);
INSERT INTO public.sample_identifier (dtype, autre, bs, ec, le, nograp, noi, nole, nolog, numfa, rges, ssech) VALUES ('InseeSampleIdentifier', '20', 20, '2', 20, '20', 20, 20, 20, 20, 20, 1);
INSERT INTO public.sample_identifier (dtype, autre, bs, ec, le, nograp, noi, nole, nolog, numfa, rges, ssech) VALUES ('InseeSampleIdentifier', '21', 21, '2', 21, '21', 21, 21, 21, 21, 21, 1);
INSERT INTO public.sample_identifier (dtype, autre, bs, ec, le, nograp, noi, nole, nolog, numfa, rges, ssech) VALUES ('InseeSampleIdentifier', '22', 22, '2', 22, '22', 22, 22, 22, 22, 22, 2);
INSERT INTO public.sample_identifier (dtype, autre, bs, ec, le, nograp, noi, nole, nolog, numfa, rges, ssech) VALUES ('InseeSampleIdentifier', '23', 23, '2', 23, '23', 23, 23, 23, 23, 23, 1);


INSERT INTO public.survey_unit (id, first_name, last_name, priority, address_id, campaign_id, interviewer_id, sample_identifier_id) VALUES ('11', 'Ted', 'Farmer', TRUE, 1, 'SIMPSONS2020X00', 'INTW1', 1);
INSERT INTO public.survey_unit (id, first_name, last_name, priority, address_id, campaign_id, interviewer_id, sample_identifier_id) VALUES ('12', 'Cecilia', 'Ortega', TRUE, 2, 'SIMPSONS2020X00', 'INTW1', 2);
INSERT INTO public.survey_unit (id, first_name, last_name, priority, address_id, campaign_id, interviewer_id, sample_identifier_id) VALUES ('13', 'Claude', 'Watkins', FALSE, 3, 'SIMPSONS2020X00', 'INTW2', 3);
INSERT INTO public.survey_unit (id, first_name, last_name, priority, address_id, campaign_id, interviewer_id, sample_identifier_id) VALUES ('14', 'Veronica', 'Gill', FALSE, 4, 'SIMPSONS2020X00', 'INTW3', 4);
INSERT INTO public.survey_unit (id, first_name, last_name, priority, address_id, campaign_id, interviewer_id, sample_identifier_id) VALUES ('20', 'Christine', 'Aguilar', TRUE, 5, 'VQS2021X00', 'INTW1', 5);
INSERT INTO public.survey_unit (id, first_name, last_name, priority, address_id, campaign_id, interviewer_id, sample_identifier_id) VALUES ('21', 'Louise', 'Walker', TRUE, 6, 'VQS2021X00', 'INTW2', 6);
INSERT INTO public.survey_unit (id, first_name, last_name, priority, address_id, campaign_id, interviewer_id, sample_identifier_id) VALUES ('22', 'Anthony', 'Bennett', FALSE, 7, 'VQS2021X00', 'INTW4', 7);
INSERT INTO public.survey_unit (id, first_name, last_name, priority, address_id, campaign_id, interviewer_id, sample_identifier_id) VALUES ('23', 'Christopher', 'Lewis', TRUE, 8, 'VQS2021X00', 'INTW4', 8);

INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('11', '+3351231231230');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('12', '+3351231231231');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('13', '+3351231231232');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('14', '+3351231231233');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('20', '+3351231231234');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('21', '+3351231231235');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('22', '+3351231231236');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('23', '+3351231231237');

INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000), 'ANS', '11');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000), 'ANS', '12');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000)+1, 'VIC', '12');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000)+2, 'PRC', '12');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000)+3, 'APS', '12');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000)+4, 'INS', '12');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000)+5, 'WFT', '12');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000)+6, 'WFS', '12');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000)+7, 'FIN', '12');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000), 'ANS', '13');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000), 'ANS', '14');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000), 'ANS', '20');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000), 'ANS', '21');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000), 'ANS', '22');
INSERT INTO public.state (date, type, survey_unit_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '3 DAY'))*1000), 'ANS', '23');

INSERT INTO public."user" (id, first_name, last_name, organization_unit_id) VALUES ('ABC', 'Melinda', 'Webb', 'OU-NORTH');
INSERT INTO public."user" (id, first_name, last_name, organization_unit_id) VALUES ('DEF', 'Everett', 'Juste', 'OU-NORTH');
INSERT INTO public."user" (id, first_name, last_name, organization_unit_id) VALUES ('GHI', 'Elsie', 'Clarke', 'OU-SOUTH');
INSERT INTO public."user" (id, first_name, last_name, organization_unit_id) VALUES ('JKL', 'Julius', 'Howell', 'OU-NATIONAL');

INSERT INTO public.preference (id_user, id_campaign) VALUES ('ABC', 'SIMPSONS2020X00');
INSERT INTO public.preference (id_user, id_campaign) VALUES ('ABC', 'VQS2021X00');
INSERT INTO public.preference (id_user, id_campaign) VALUES ('DEF', 'SIMPSONS2020X00');
INSERT INTO public.preference (id_user, id_campaign) VALUES ('DEF', 'VQS2021X00');
INSERT INTO public.preference (id_user, id_campaign) VALUES ('GHI', 'VQS2021X00');

INSERT INTO public.visibility (collection_end_date, collection_start_date, organization_unit_id, campaign_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '2 DAY'))*1000), (SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '1 DAY'))*1000), 'OU-NORTH', 'SIMPSONS2020X00');
INSERT INTO public.visibility (collection_end_date, collection_start_date, organization_unit_id, campaign_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '2 DAY'))*1000), (SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '1 DAY'))*1000), 'OU-NORTH', 'VQS2021X00');
INSERT INTO public.visibility (collection_end_date, collection_start_date, organization_unit_id, campaign_id) VALUES ((SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '2 DAY'))*1000), (SELECT extract(epoch from (LOCALTIMESTAMP - INTERVAL '1 DAY'))*1000), 'OU-SOUTH', 'VQS2021X00');
