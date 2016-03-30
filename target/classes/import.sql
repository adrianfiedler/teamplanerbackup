--
-- JBoss, Home of Professional Open Source
-- Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
-- contributors by the @authors tag. See the copyright.txt in the
-- distribution for a full listing of individual contributors.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- http://www.apache.org/licenses/LICENSE-2.0
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

-- You can use this file to load seed data into the database using SQL statements
insert into UserSettings (id, montagsAbsagen, dienstagsAbsagen, mittwochsAbsagen, donnerstagsAbsagen, freitagsAbsagen, samstagsAbsagen, sonntagsAbsagen) values (-1, false, false, false, false, false, false, false) 
insert into UserSettings (id, montagsAbsagen, dienstagsAbsagen, mittwochsAbsagen, donnerstagsAbsagen, freitagsAbsagen, samstagsAbsagen, sonntagsAbsagen) values (-2, false, false, false, false, false, false, false) 


insert into Verein (id, name) values (-1, 'TSV Solln') 
insert into User (id, email, facebooktoken, facebookUserId, vorname, name, passwort, admin, aktiviert, aktivierToken, weeklyStatusMail, terminReminderMail, user_settings_ref, verein_ref) values (-1, 'julischka@onlinehome.de', NULL, NULL, 'Jule', 'Fiedler', 'test', true, true, 'token', true, true, -1, -1)
insert into User (id, email, facebooktoken, facebookUserId, vorname, name, passwort, admin, aktiviert, aktivierToken, weeklyStatusMail, terminReminderMail, user_settings_ref, verein_ref) values (-2, 'adrian_fiedler@msn.com', NULL, NULL, 'Adrian', 'Fiedler', 'test', true, true, 'token', true, true, -2, -1)
insert into Team (id, name, verein_ref) values (-1, 'Team1', -1)
insert into TeamRolle (id, rolle, user_ref, team_ref, inTeam) values (-1, 'Trainer', -1, -1, true)
insert into TeamRolle (id, rolle, user_ref, team_ref, inTeam) values (-2, 'Trainer', -2, -1, true)

insert into Team (id, name, verein_ref) values (-2, 'Team2', -1)
insert into TeamRolle (id, rolle, user_ref, team_ref, inTeam) values (-3, 'Trainer', -1, -2, true)

insert into Ort (id, beschreibung, plz, stadt, strasse, nummer, vorlage, latitude, longitude, verein_ref) values (-1, 'Heim Halle', '81475', 'Muenchen', 'Herterichstrasse', '139', true, 48.080468, 11.499757,-1)
insert into TerminVorlage (id, name, beschreibung, time, ort_ref, team_ref) values (-1, 'Training Vorlage', 'Beschreibung', '2015-10-24 18:00:00', -1, -1)
insert into Termin (id, beschreibung, absageKommentar, datum, name, ort_ref, serie_ref, team_ref, status, maybeAllowed, defaultZusageStatus) values (-1, 'Training', NULL, '2016-02-27 18:00:00', 'Training', -1, NULL, -1, 1, true, 0)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-1, 'Jules Kommentar 1', 0, false, -1, -1)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-2, 'Adrians Kommentar 1', 1, false, -2, -1)

insert into Termin (id, beschreibung, absageKommentar, datum, name, ort_ref, serie_ref, team_ref, status, maybeAllowed, defaultZusageStatus) values (-2, 'Spiel 1', NULL, '2016-03-03 14:30:00', 'Spiel 1', -1, NULL, -1, 1, false, 0)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-3, 'Jules Kommentar 2', 0, false, -1, -2)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-4, 'Adrians Kommentar 2', 1, false, -2, -2)

insert into Termin (id, beschreibung, absageKommentar, datum, name, ort_ref, serie_ref, team_ref, status, maybeAllowed, defaultZusageStatus) values (-3, 'Spiel 2', NULL, '2016-03-10 18:00:00', 'Spiel 2', -1, NULL, -1, 1, true, 0)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-5, 'Jules Kommentar 3', 0, false, -1, -3)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-6, 'Adrians Kommentar 3', 1, false, -2, -3)

insert into Termin (id, beschreibung, absageKommentar, datum, name, ort_ref, serie_ref, team_ref, status, maybeAllowed, defaultZusageStatus) values (-4, 'Spiel Team 2', NULL, '2016-03-18 18:00:00', 'Spiel 1 team 2', -1, NULL, -2, 1, false, 0)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-7, 'Termin Team 2 Kommentar Jule', 0, false, -1, -4)
