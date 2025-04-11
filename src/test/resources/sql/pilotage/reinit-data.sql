ALTER TABLE public.communication_request_status DISABLE TRIGGER ALL;
ALTER TABLE public.communication_request DISABLE TRIGGER ALL;
ALTER TABLE public.communication_template DISABLE TRIGGER ALL;
ALTER TABLE public.contact_attempt DISABLE TRIGGER ALL;
ALTER TABLE public.referent DISABLE TRIGGER ALL;
ALTER TABLE public.message DISABLE TRIGGER ALL;
ALTER TABLE public.sample_identifier DISABLE TRIGGER ALL;
ALTER TABLE public.identification DISABLE TRIGGER ALL;
ALTER TABLE public.person DISABLE TRIGGER ALL;
ALTER TABLE public.phone_number DISABLE TRIGGER ALL;
ALTER TABLE public.state DISABLE TRIGGER ALL;
ALTER TABLE public.contact_outcome DISABLE TRIGGER ALL;
ALTER TABLE public.comment DISABLE TRIGGER ALL;
ALTER TABLE public.closing_cause DISABLE TRIGGER ALL;
ALTER TABLE public.address DISABLE TRIGGER ALL;

TRUNCATE TABLE public.communication_request_status CASCADE;
TRUNCATE TABLE public.communication_request CASCADE;
TRUNCATE TABLE public.communication_template CASCADE;
TRUNCATE TABLE public.campaign_message_recipient CASCADE;
TRUNCATE TABLE public.contact_attempt CASCADE;
TRUNCATE TABLE public.message_status CASCADE;
TRUNCATE TABLE public.oumessage_recipient CASCADE;
TRUNCATE TABLE public.referent CASCADE;
TRUNCATE TABLE public.message CASCADE;
TRUNCATE TABLE public.interviewer CASCADE;
TRUNCATE TABLE public.sample_identifier CASCADE;
TRUNCATE TABLE public.user CASCADE;
TRUNCATE TABLE public.campaign CASCADE;
TRUNCATE TABLE public.preference CASCADE;
TRUNCATE TABLE public.visibility CASCADE;
TRUNCATE TABLE public.survey_unit CASCADE;
TRUNCATE TABLE public.identification CASCADE;
TRUNCATE TABLE public.person CASCADE;
TRUNCATE TABLE public.phone_number CASCADE;
TRUNCATE TABLE public.state CASCADE;
TRUNCATE TABLE public.contact_outcome CASCADE;
TRUNCATE TABLE public.comment CASCADE;
TRUNCATE TABLE public.closing_cause CASCADE;
TRUNCATE TABLE public.organization_unit CASCADE;
TRUNCATE TABLE public.address CASCADE;

ALTER TABLE public.communication_request_status ALTER COLUMN id RESTART WITH 1;
ALTER TABLE public.communication_request ALTER COLUMN id RESTART WITH 1;
ALTER TABLE public.contact_attempt ALTER COLUMN id RESTART WITH 1;
ALTER TABLE public.referent ALTER COLUMN id RESTART WITH 1;
ALTER TABLE public.message ALTER COLUMN id RESTART WITH 1;
ALTER TABLE public.sample_identifier ALTER COLUMN id RESTART WITH 1;
ALTER TABLE public.identification ALTER COLUMN id RESTART WITH 1;
ALTER TABLE public.person ALTER COLUMN id RESTART WITH 1;
ALTER TABLE public.phone_number ALTER COLUMN id RESTART WITH 1;
ALTER TABLE public.state ALTER COLUMN id RESTART WITH 1;
ALTER TABLE public.contact_outcome ALTER COLUMN id RESTART WITH 1;
ALTER TABLE public.comment ALTER COLUMN id RESTART WITH 1;
ALTER TABLE public.closing_cause ALTER COLUMN id RESTART WITH 1;
ALTER TABLE public.address ALTER COLUMN id RESTART WITH 1;

INSERT INTO public.address (dtype,l1,l2,l3,l4,l5,l6,l7,building,floor,door,staircase,elevator,city_priority_district) VALUES
	 ('InseeAddress','Ted Farmer','','','1 rue de la gare','','29270 Carhaix','France',NULL,NULL,NULL,NULL,NULL,NULL),
	 ('InseeAddress','Cecilia Ortega','','','2 place de la mairie','','90000 Belfort','France',NULL,NULL,NULL,NULL,NULL,NULL),
	 ('InseeAddress','Claude Watkins','','','3 avenue de la République','','32230 Marciac','France',NULL,NULL,NULL,NULL,NULL,NULL),
	 ('InseeAddress','Veronica Gill','','','4 chemin du ruisseau','','44190 Clisson','France','','','','',true,true),
	 ('InseeAddress','Christine Aguilar','','','5 rue de l''école','','59620 Aulnoye-Aimeries','France','','','','',true,true),
	 ('InseeAddress','Louise Walker','','','6 impasse du lac','','38200 Vienne','France','','','','',true,true),
	 ('InseeAddress','Anthony Bennett','','','7 avenue de la Liberté','','62000 Arras','France','','','','',true,true),
	 ('InseeAddress','Christopher Lewis','','','8 route du moulin','','35000 Rennes','France','','','','',true,true),
	 ('InseeAddress','Veronica Gill','','','4 chemin du ruisseau','','44190 Clisson','France','','','','',true,true),
	 ('InseeAddress','Christine Aguilar','','','5 rue de l''école','','59620 Aulnoye-Aimeries','France','','','','',true,true),
	 ('InseeAddress','Louise Walker','','','6 impasse du lac','','38200 Vienne','France','','','','',true,true),
	 ('InseeAddress','Anthony Bennett','','','7 avenue de la Liberté','','62000 Arras','France','','','','',true,true);

INSERT INTO public.campaign (id,"label",email,identification_configuration,contact_attempt_configuration,contact_outcome_configuration) VALUES
	 ('SIMPSONS2020X00','Survey on the Simpsons tv show 2020',NULL,'IASCO','F2F','F2F'),
	 ('VQS2021X00','Everyday life and health survey 2021',NULL,'IASCO','F2F','F2F'),
	 ('STATE2020X00','Everyday life and health survey 2021',NULL,'IASCO','F2F','F2F'),
	 ('STATE2021X00','Everyday life and health survey 2021',NULL,'IASCO','F2F','F2F'),
	 ('STATE2022X00','Everyday life and health survey 2021',NULL,'IASCO','F2F','F2F'),
	 ('STATE2023X00','Everyday life and health survey 2021',NULL,'IASCO','F2F','F2F'),
	 ('STATE2024X00','Everyday life and health survey 2021',NULL,'IASCO','F2F','F2F');
INSERT INTO public.communication_template (meshuggah_id, medium, type, campaign_id) VALUES
	 ('meshuggahId1','LETTER','REMINDER', 'SIMPSONS2020X00');
INSERT INTO communication_request ( survey_unit_id, campaign_id, meshuggah_id, reason, emitter) VALUES
     ('11', 'SIMPSONS2020X00', 'READY', 'meshuggahId1', 'REFUSAL');
INSERT INTO communication_request_status (communication_request_id, status, "date") VALUES
     (1,'READY', 1590969600000);

INSERT INTO public.message ("date","text",sender_id) VALUES
	 (1602168871000,'test','ABC'),
	 (1602168871000,'test','ABC'),
	 (1602168871000,'test','ABC'),
	 (1602168871000,'test','ABC'),
	 (1602168871000,'test','ABC'),
	 (2548853671000,'test','ABC');
INSERT INTO public.campaign_message_recipient (campaign_id,message_id) VALUES
	 ('SIMPSONS2020X00',1),
	 ('SIMPSONS2020X00',4),
	 ('SIMPSONS2020X00',5);
INSERT INTO public.closing_cause ("date","type",survey_unit_id) VALUES
	 (1621706851845,'NPI','12'),
	 (1621706851845,'NPI','14');
INSERT INTO public.interviewer (id,email,first_name,last_name,phone_number,title) VALUES
	 ('INTW1','margie.lucas@ou.com','Margie','Lucas','+3391231231230','MISTER'),
	 ('INTW2','carlton.campbell@ou.com','Carlton','Campbell','+3391231231231','MISTER'),
	 ('INTW3','gerald.edwards@ou.com','Gerald','Edwards','+3391231231231','MISTER'),
	 ('INTW4','melody.grant@ou.com','Melody','Grant','+3391231231231','MISTER');
INSERT INTO public.message_status (interviewer_id,message_id,status) VALUES
	 ('INTW1',4,'2'),
	 ('INTW1',5,'2');
INSERT INTO public.organization_unit (id,"label","type",organization_unit_parent_id) VALUES
	 ('OU-NATIONAL','National organizational unit','NATIONAL',NULL),
	 ('OU-NORTH','North region organizational unit','LOCAL','OU-NATIONAL'),
	 ('OU-SOUTH','South region organizational unit','LOCAL','OU-NATIONAL');
INSERT INTO public.oumessage_recipient (message_id,organization_unit_id) VALUES
	 (2,'OU-NORTH'),
	 (5,'OU-NORTH');
INSERT INTO public.sample_identifier (dtype,autre,bs,ec,le,nograp,noi,nole,nolog,numfa,rges,ssech) VALUES
	 ('InseeSampleIdentifier','11',11,'1',11,'11',11,11,11,11,11,1),
	 ('InseeSampleIdentifier','12',12,'1',12,'12',12,12,12,12,12,1),
	 ('InseeSampleIdentifier','13',13,'1',13,'13',13,13,13,13,13,2),
	 ('InseeSampleIdentifier','14',14,'1',14,'14',14,14,14,14,14,3),
	 ('InseeSampleIdentifier','20',20,'2',20,'20',20,20,20,20,20,1),
	 ('InseeSampleIdentifier','21',21,'2',21,'21',21,21,21,21,21,1),
	 ('InseeSampleIdentifier','22',22,'2',22,'22',22,22,22,22,22,2),
	 ('InseeSampleIdentifier','23',23,'2',23,'23',23,23,23,23,23,1),
	 ('InseeSampleIdentifier','14',14,'1',14,'14',14,14,14,14,14,3),
	 ('InseeSampleIdentifier','20',20,'2',20,'20',20,20,20,20,20,1),
	 ('InseeSampleIdentifier','21',21,'2',21,'21',21,21,21,21,21,1),
	 ('InseeSampleIdentifier','22',22,'2',22,'22',22,22,22,22,22,2),
	 ('InseeSampleIdentifier','23',23,'2',23,'23',23,23,23,23,23,1);
INSERT INTO public.state ("date","type",survey_unit_id) VALUES
	 (1590504459838,'NVM','11'),
	 (1590504468838,'NVM','12'),
	 (1590504472342,'NNS','13'),
	 (1590504478334,'NNS','14'),
	 (1590504478334,'NVM','20'),
	 (1590504478334,'NNS','21'),
	 (1590504478334,'NNS','22'),
	 (1590504478334,'NNS','23'),
	 (1590504478334,'NVM','24'),
	 (1590504478334,'NVM','25'),
	 (1590504478334,'NVM','26'),
	 (1590504478334,'NVM','27'),
	 (1590504478334,'NVM','28');
INSERT INTO public.survey_unit (id, display_name, priority,address_id,campaign_id,interviewer_id,sample_identifier_id,organization_unit_id,viewed,"move") VALUES
	 ('11','business-id-11',true,1,'SIMPSONS2020X00','INTW1',1,'OU-NORTH',false,NULL),
	 ('12','business-id-12',true,2,'SIMPSONS2020X00','INTW1',2,'OU-NORTH',false,NULL),
	 ('13','business-id-13',false,3,'SIMPSONS2020X00','INTW2',3,'OU-SOUTH',false,NULL),
	 ('14','business-id-14',false,4,'SIMPSONS2020X00','INTW3',4,'OU-SOUTH',false,NULL),
	 ('20','business-id-20',true,5,'VQS2021X00','INTW1',5,'OU-NORTH',false,NULL),
	 ('21','business-id-21',true,6,'VQS2021X00','INTW2',6,'OU-NORTH',false,NULL),
	 ('22','business-id-22',false,7,'VQS2021X00','INTW4',7,'OU-NORTH',false,NULL),
	 ('23','business-id-23',true,8,'VQS2021X00','INTW4',8,'OU-NORTH',false,NULL),
	 ('24','business-id-24',false,9,'STATE2020X00','INTW1',9,'OU-NORTH',false,NULL),
	 ('25','business-id-25',true,10,'STATE2021X00','INTW1',10,'OU-NORTH',false,NULL),
	 ('26','business-id-26',true,11,'STATE2022X00','INTW1',11,'OU-NORTH',false,NULL),
	 ('27','business-id-27',false,12,'STATE2023X00','INTW1',12,'OU-NORTH',false,NULL),
	 ('28','business-id-28',true,13,'STATE2024X00','INTW1',13,'OU-NORTH',false,NULL);
INSERT INTO public."user" (id,first_name,last_name,organization_unit_id) VALUES
	 ('ABC','Melinda','Webb','OU-NORTH'),
	 ('DEF','Everett','Juste','OU-NORTH'),
	 ('GHI','Elsie','Clarke','OU-SOUTH'),
	 ('JKL','Julius','Howell','OU-NATIONAL');
INSERT INTO public.visibility (campaign_id,organization_unit_id,collection_end_date,collection_start_date,end_date,identification_phase_start_date,interviewer_start_date,management_start_date,use_letter_communication,mail,tel) VALUES
	 ('SIMPSONS2020X00','OU-NORTH',1640995200000,1645995200000,1641513600000,1577232000000,1576800000000,1575936000000,true,'mail@ma.il','0123456789'),
	 ('VQS2021X00','OU-NORTH',1577836800000,1577836800000,1577836800000,1577232000000,1576800000000,1575936000000,true,NULL,NULL),
	 ('VQS2021X00','OU-SOUTH',1640995200000,1577836800000,1641513600000,1577232000000,1576800000000,1575936000000,true,NULL,NULL),
	 ('STATE2020X00','OU-NORTH',1640995200000,1640995200000,1640995200000,1640995200000,1640995200000,1640995200000,true,NULL,NULL),
	 ('STATE2021X00','OU-NORTH',1641513600000,1577232000000,1641513600000,1640995200000,1640995200000,1577232000000,true,NULL,NULL),
	 ('STATE2022X00','OU-NORTH',1640995200000,1577232000000,1640995200000,1640995200000,1577232000000,1577232000000,true,NULL,NULL),
	 ('STATE2023X00','OU-NORTH',1577232000000,1577232000000,1640995200000,1577232000000,1576800000000,1575936000000,true,NULL,NULL),
	 ('STATE2024X00','OU-NORTH',1577232000000,1577232000000,1577232000000,1577232000000,1577232000000,1577232000000,true,NULL,NULL);


ALTER TABLE public.communication_request_status ENABLE TRIGGER ALL;
ALTER TABLE public.communication_request ENABLE TRIGGER ALL;
ALTER TABLE public.communication_template ENABLE TRIGGER ALL;
ALTER TABLE public.contact_attempt ENABLE TRIGGER ALL;
ALTER TABLE public.referent ENABLE TRIGGER ALL;
ALTER TABLE public.message ENABLE TRIGGER ALL;
ALTER TABLE public.sample_identifier ENABLE TRIGGER ALL;
ALTER TABLE public.identification ENABLE TRIGGER ALL;
ALTER TABLE public.person ENABLE TRIGGER ALL;
ALTER TABLE public.phone_number ENABLE TRIGGER ALL;
ALTER TABLE public.state ENABLE TRIGGER ALL;
ALTER TABLE public.contact_outcome ENABLE TRIGGER ALL;
ALTER TABLE public.comment ENABLE TRIGGER ALL;
ALTER TABLE public.closing_cause ENABLE TRIGGER ALL;
ALTER TABLE public.address ENABLE TRIGGER ALL;
