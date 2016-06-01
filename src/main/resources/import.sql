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

insert into Verein (id, name, gekaufteTeams) values (-1, 'TSV Solln', 3) 
insert into User (id, email, facebooktoken, facebookUserId, vorname, name, passwort, admin, aktiviert, aktivierToken, weeklyStatusMail, terminReminderMail, timeZone, user_settings_ref, verein_ref) values (-1, 'BMUNQCMF/zJYU8iUmpBEQ2kChL9jSyiz', NULL, NULL, '3I0kAis4SF4=', '8M5tvTujOTM=', 'GQJOE/6AiWA=', true, true, 'token', true, true, 'Europe/Berlin', -1, -1)
insert into User (id, email, facebooktoken, facebookUserId, vorname, name, passwort, admin, aktiviert, aktivierToken, weeklyStatusMail, terminReminderMail, timeZone, user_settings_ref, verein_ref) values (-2, 'DKRxLb8uf06dZnkHTa8i2mE11Kykc3Nn', NULL, NULL, '3EWzbZFGbsM=', '8M5tvTujOTM=', 'GQJOE/6AiWA=', true, true, 'token', true, true, 'Europe/Berlin', -2, -1)
insert into Team (id, name, verein_ref) values (-1, 'Team1', -1)
insert into TeamRolle (id, rolle, user_ref, team_ref, inTeam) values (-1, 'Trainer', -1, -1, true)
insert into TeamRolle (id, rolle, user_ref, team_ref, inTeam) values (-2, 'Trainer', -2, -1, true)

insert into Team (id, name, verein_ref) values (-2, 'Team2', -1)
insert into TeamRolle (id, rolle, user_ref, team_ref, inTeam) values (-3, 'Trainer', -1, -2, true)

insert into Ort (id, beschreibung, plz, stadt, strasse, nummer, vorlage, latitude, longitude, verein_ref) values (-1, 'Heim Halle', '81475', 'Muenchen', 'Herterichstrasse', '139', true, 48.080468, 11.499757,-1)
insert into TerminVorlage (id, name, beschreibung, time, ort_ref, team_ref) values (-1, 'Training Vorlage', 'Beschreibung', '2015-10-24 18:00:00', -1, -1)
insert into Termin (id, beschreibung, absageKommentar, datum, name, ort_ref, serie_ref, team_ref, status, maybeAllowed, defaultZusageStatus) values (-1, 'Training', NULL, '2016-05-25 08:00:00', 'Training', -1, NULL, -1, 1, true, 0)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-1, 'Jules Kommentar 1', 0, false, -1, -1)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-2, 'Adrians Kommentar 1', 1, false, -2, -1)

insert into Termin (id, beschreibung, absageKommentar, datum, name, ort_ref, serie_ref, team_ref, status, maybeAllowed, defaultZusageStatus) values (-2, 'Spiel 1', NULL, '2016-05-27 09:00:00', 'Spiel 1', -1, NULL, -1, 1, false, 0)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-3, 'Jules Kommentar 2', 0, false, -1, -2)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-4, 'Adrians Kommentar 2', 1, false, -2, -2)

insert into Termin (id, beschreibung, absageKommentar, datum, name, ort_ref, serie_ref, team_ref, status, maybeAllowed, defaultZusageStatus) values (-3, 'Spiel 2', NULL, '2016-05-29 10:00:00', 'Spiel 2', -1, NULL, -1, 1, true, 0)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-5, 'Jules Kommentar 3', 0, false, -1, -3)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-6, 'Adrians Kommentar 3', 1, false, -2, -3)

insert into Termin (id, beschreibung, absageKommentar, datum, name, ort_ref, serie_ref, team_ref, status, maybeAllowed, defaultZusageStatus) values (-4, 'Spiel Team 2', NULL, '2016-05-31 11:00:00', 'Spiel 1 team 2', -1, NULL, -2, 1, false, 0)
insert into Zusage (id, kommentar, status, autoSet, user_ref, termin_ref) values (-7, 'Termin Team 2 Kommentar Jule', 0, false, -1, -4)

insert into TeamMailSettings (id, mailText, showMailText, showIntroduction, hoursBeforeTrainerReminder, team_ref) values (-1, 'Hier steht der MailText\nNeue Zeile', true, true, 2, -1)
insert into TeamMailSettings (id, mailText, showMailText, showIntroduction, hoursBeforeTrainerReminder, team_ref) values (-2, 'Hier steht der MailText\nNeue Zeile', true, true, 2, -2)
insert into VereinModule (id, mailModul, verein_ref) values (-1, true, -1)
insert into TeamSettings (id, trainerMussZusagen, team_ref) values (-1,  true, -1)
insert into TeamSettings (id, trainerMussZusagen, team_ref) values (-2, false, -2)

insert into News (id, title, text, ersteller, category, pictureUrl, creationDate, modificationDate) values (-1, "Erste News", "Hier ist der Text der ersten News", "Adrian", "Allgemeines", "https://upload.wikimedia.org/wikipedia/commons/1/13/Nuvola_filesystems_folder_home.png", "2016-05-31 11:00:00", "2016-05-31 11:00:00")
insert into News (id, title, text, ersteller, category, pictureUrl, creationDate, modificationDate) values (-2, "Zweite News", "Hier ist der Text der zweiten News", "Masalis", "Info", null, "2016-06-01 20:00:00", "2016-06-01 20:00:00")
insert into News (id, title, text, ersteller, category, pictureUrl, creationDate, modificationDate) values (-3, "Dritte News", "Hier ist der Text der dritten News", "Jule", "Allgemeines", "https://upload.wikimedia.org/wikipedia/commons/2/2b/Nuvola_apps_konquest.png", "2016-06-01 21:00:00", "2016-06-01 21:00:00")
insert into News (id, title, text, ersteller, category, pictureUrl, creationDate, modificationDate) values (-4, "Vierte News", "Hier ist der Text der vierten News", "Weihnachtsmann", "Wartungsarbeiten", null, "2016-06-02 12:00:00", "2016-06-02 12:00:00")