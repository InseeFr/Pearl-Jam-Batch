INSERT INTO public.campaign (id, end_date, start_date, label) VALUES ('SIMPSONS2020X00', 1640995200000, 1577836800000, 'Survey on the Simpsons tv show 2020');
INSERT INTO public.campaign (id, end_date, start_date, label) VALUES ('VQS2021X00', 1640995200000, 1577836800000, 'Everyday life and health survey 2021');
INSERT INTO public.campaign (id, end_date, start_date, label) VALUES ('STATE2020X00', 1640995200000, 1577836800000, 'Everyday life and health survey 2021');
INSERT INTO public.campaign (id, end_date, start_date, label) VALUES ('STATE2021X00', 1640995200000, 1577836800000, 'Everyday life and health survey 2021');
INSERT INTO public.campaign (id, end_date, start_date, label) VALUES ('STATE2022X00', 1640995200000, 1577836800000, 'Everyday life and health survey 2021');
INSERT INTO public.campaign (id, end_date, start_date, label) VALUES ('STATE2023X00', 1640995200000, 1577836800000, 'Everyday life and health survey 2021');
INSERT INTO public.campaign (id, end_date, start_date, label) VALUES ('STATE2024X00', 1640995200000, 1577836800000, 'Everyday life and health survey 2021');

INSERT INTO public.address (dtype, l1, l2, l3, l4, l5, l6, l7, geographical_location_id) VALUES ('InseeAddress', 'Veronica Gill' ,'','','4 chemin du ruisseau' ,'','44190 Clisson' ,'France', '44043');
INSERT INTO public.address (dtype, l1, l2, l3, l4, l5, l6, l7, geographical_location_id) VALUES ('InseeAddress', 'Christine Aguilar' ,'','','5 rue de l''école' ,'','59620 Aulnoye-Aimeries' ,'France', '59033');
INSERT INTO public.address (dtype, l1, l2, l3, l4, l5, l6, l7, geographical_location_id) VALUES ('InseeAddress', 'Louise Walker' ,'','','6 impasse du lac' ,'','38200 Vienne' ,'France', '38544');
INSERT INTO public.address (dtype, l1, l2, l3, l4, l5, l6, l7, geographical_location_id) VALUES ('InseeAddress', 'Anthony Bennett' ,'','','7 avenue de la Liberté' ,'','62000 Arras' ,'France', '62041');
INSERT INTO public.address (dtype, l1, l2, l3, l4, l5, l6, l7, geographical_location_id) VALUES ('InseeAddress', 'Christopher Lewis' ,'','','8 route du moulin' ,'','35000 Rennes' ,'France', '35238');

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
INSERT INTO public.survey_unit (id, first_name, last_name, priority, address_id, campaign_id, interviewer_id, sample_identifier_id) VALUES ('24', 'Veronica', 'Gill', FALSE, 9, 'STATE2020X00', 'INTW1', 9);
INSERT INTO public.survey_unit (id, first_name, last_name, priority, address_id, campaign_id, interviewer_id, sample_identifier_id) VALUES ('25', 'Christine', 'Aguilar', TRUE, 10, 'STATE2021X00', 'INTW1', 10);
INSERT INTO public.survey_unit (id, first_name, last_name, priority, address_id, campaign_id, interviewer_id, sample_identifier_id) VALUES ('26', 'Louise', 'Walker', TRUE, 11, 'STATE2022X00', 'INTW1', 11);
INSERT INTO public.survey_unit (id, first_name, last_name, priority, address_id, campaign_id, interviewer_id, sample_identifier_id) VALUES ('27', 'Anthony', 'Bennett', FALSE, 12, 'STATE2023X00', 'INTW1', 12);
INSERT INTO public.survey_unit (id, first_name, last_name, priority, address_id, campaign_id, interviewer_id, sample_identifier_id) VALUES ('28', 'Christopher', 'Lewis', TRUE, 13, 'STATE2024X00', 'INTW1', 13);

INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('11', '+3351231231230');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('12', '+3351231231231');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('13', '+3351231231232');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('14', '+3351231231233');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('20', '+3351231231234');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('21', '+3351231231235');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('22', '+3351231231236');
INSERT INTO public.survey_unit_phone_numbers(survey_unit_id, phone_numbers) VALUES ('23', '+3351231231237');

INSERT INTO public.state (date, type, survey_unit_id) VALUES (1590504459838, 'NVM', '11');
INSERT INTO public.state (date, type, survey_unit_id) VALUES (1590504468838, 'NVM', '12');
INSERT INTO public.state (date, type, survey_unit_id) VALUES (1590504472342, 'NNS', '13');
INSERT INTO public.state (date, type, survey_unit_id) VALUES (1590504478334, 'NNS', '14');
INSERT INTO public.state (date, type, survey_unit_id) VALUES (1590504478334, 'NVM', '20');
INSERT INTO public.state (date, type, survey_unit_id) VALUES (1590504478334, 'NNS', '21');
INSERT INTO public.state (date, type, survey_unit_id) VALUES (1590504478334, 'NNS', '22');
INSERT INTO public.state (date, type, survey_unit_id) VALUES (1590504478334, 'NNS', '23');

INSERT INTO public.state (date, type, survey_unit_id) VALUES (1590504478334, 'NVM', '24');
INSERT INTO public.state (date, type, survey_unit_id) VALUES (1590504478334, 'NVM', '25');
INSERT INTO public.state (date, type, survey_unit_id) VALUES (1590504478334, 'NVM', '26');
INSERT INTO public.state (date, type, survey_unit_id) VALUES (1590504478334, 'NVM', '27');
INSERT INTO public.state (date, type, survey_unit_id) VALUES (1590504478334, 'NVM', '28');

INSERT INTO public.visibility(organization_unit_id, campaign_id, collection_end_date, collection_start_date, end_date, identification_phase_start_date, interviewer_start_date, management_start_date) VALUES ('OU-NORTH', 'SIMPSONS2020X00',  1640995200000, 1645995200000, 1641513600000, 1577232000000, 1576800000000, 1575936000000);
INSERT INTO public.visibility(organization_unit_id, campaign_id, collection_end_date, collection_start_date, end_date, identification_phase_start_date, interviewer_start_date, management_start_date) VALUES ('OU-NORTH', 'VQS2021X00',  1577836800000, 1577836800000, 1577836800000, 1577232000000, 1576800000000, 1575936000000);
INSERT INTO public.visibility(organization_unit_id, campaign_id, collection_end_date, collection_start_date, end_date, identification_phase_start_date, interviewer_start_date, management_start_date) VALUES ('OU-SOUTH', 'VQS2021X00',  1640995200000, 1577836800000, 1641513600000, 1577232000000, 1576800000000, 1575936000000);

INSERT INTO public.visibility(organization_unit_id, campaign_id, collection_end_date, collection_start_date, end_date, identification_phase_start_date, interviewer_start_date, management_start_date) VALUES ('OU-NORTH', 'STATE2020X00',  1640995200000, 1640995200000, 1640995200000, 1640995200000, 1640995200000, 1640995200000);
INSERT INTO public.visibility(organization_unit_id, campaign_id, collection_end_date, collection_start_date, end_date, identification_phase_start_date, interviewer_start_date, management_start_date) VALUES ('OU-NORTH', 'STATE2021X00',  1641513600000, 1577232000000, 1641513600000, 1640995200000, 1640995200000, 1577232000000);
INSERT INTO public.visibility(organization_unit_id, campaign_id, collection_end_date, collection_start_date, end_date, identification_phase_start_date, interviewer_start_date, management_start_date) VALUES ('OU-NORTH', 'STATE2022X00',  1640995200000, 1577232000000, 1640995200000, 1640995200000, 1577232000000, 1577232000000);
INSERT INTO public.visibility(organization_unit_id, campaign_id, collection_end_date, collection_start_date, end_date, identification_phase_start_date, interviewer_start_date, management_start_date) VALUES ('OU-NORTH', 'STATE2023X00',  1577232000000, 1577232000000, 1640995200000, 1577232000000, 1576800000000, 1575936000000);
INSERT INTO public.visibility(organization_unit_id, campaign_id, collection_end_date, collection_start_date, end_date, identification_phase_start_date, interviewer_start_date, management_start_date) VALUES ('OU-NORTH', 'STATE2024X00',  1577232000000, 1577232000000, 1577232000000, 1577232000000, 1577232000000, 1577232000000);

INSERT INTO public.message(id, date, text, sender_id) VALUES (1, 1602168871000, 'test', 'ABC');
INSERT INTO public.message(id, date, text, sender_id) VALUES (2, 1602168871000, 'test', 'ABC');
INSERT INTO public.message(id, date, text, sender_id) VALUES (3, 1602168871000, 'test', 'ABC');
INSERT INTO public.message(id, date, text, sender_id) VALUES (4, 1602168871000, 'test', 'ABC');
INSERT INTO public.message(id, date, text, sender_id) VALUES (5, 1602168871000, 'test', 'ABC');
INSERT INTO public.message(id, date, text, sender_id) VALUES (6, 2548853671000, 'test', 'ABC');

INSERT INTO public.campaign_message_recipient(campaign_id, message_id) VALUES ('SIMPSONS2020X00', 1);
INSERT INTO public.oumessage_recipient(organization_unit_id, message_id) VALUES ('OU-NORTH', 2);
INSERT INTO public.campaign_message_recipient(campaign_id, message_id) VALUES ('SIMPSONS2020X00', 4);
INSERT INTO public.message_status(interviewer_id, message_id, status) VALUES ('INTW1', 4, 2);
INSERT INTO public.campaign_message_recipient(campaign_id, message_id) VALUES ('SIMPSONS2020X00', 5);
INSERT INTO public.oumessage_recipient(organization_unit_id, message_id) VALUES ('OU-NORTH', 5);
INSERT INTO public.message_status(interviewer_id, message_id, status) VALUES ('INTW1', 5, 2);