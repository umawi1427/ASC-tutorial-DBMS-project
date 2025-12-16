create database ASCTutorialCenter;

use ASCTutorialCenter;

CREATE TABLE Person (
    SystemID INT PRIMARY KEY,
    FirstName VARCHAR(100),
    LastName VARCHAR(100),
    Email VARCHAR(100),
    PhoneNum VARCHAR(25),
    UserName VARCHAR(50),
    Password VARCHAR(50)
);

CREATE TABLE Student (
    SystemID INT PRIMARY KEY,
    PreferredLanguage VARCHAR(100),
    FOREIGN KEY (SystemID)
        REFERENCES person (SystemID)
);

CREATE TABLE Manager (
    SystemID INT PRIMARY KEY,
    DateHired DATE,
    FOREIGN KEY (SystemID)
        REFERENCES person (SystemID)
);

CREATE TABLE Tutor (
    SystemID INT PRIMARY KEY,
    DateHired DATE,
    ManagedBy INT,
    FOREIGN KEY (SystemID)
        REFERENCES Student (SystemID),
    FOREIGN KEY (ManagedBy)
        REFERENCES Manager (SystemID)
);

CREATE TABLE SubjectsOffered (
    SubjectID INT PRIMARY KEY,
    SubjectName VARCHAR(100)
);

CREATE TABLE Expertise (
    SystemID INT,
    SubjectID INT,
    YearsOfExperience INT,
    PRIMARY KEY (SystemID , SubjectID),
    FOREIGN KEY (SystemID)
        REFERENCES Tutor (SystemID),
    FOREIGN KEY (SubjectID)
        REFERENCES SubjectsOffered (SubjectID)
);

CREATE TABLE AvailableLanguage (
    LanguageID INT PRIMARY KEY,
    Language VARCHAR(100)
);


CREATE TABLE Fluency (
    LanguageID INT,
    SystemID INT,
    Fluent VARCHAR(20),
    PRIMARY KEY (LanguageID , SystemID),
    FOREIGN KEY (LanguageID)
        REFERENCES AvailableLanguage (LanguageID),
    FOREIGN KEY (SystemID)
        REFERENCES Tutor (SystemID)
);

CREATE TABLE Session (
    SessionID INT PRIMARY KEY,
    SessionDate DATE,
    SessionTime TIME,
    Location VARCHAR(50),
    StudLim INT,
    SubjectID INT,
    SystemID INT,
    LanguageID INT,
    FOREIGN KEY (SubjectID)
        REFERENCES SubjectsOffered (SubjectID),
    FOREIGN KEY (SystemID)
        REFERENCES Tutor (SystemID),
    FOREIGN KEY (LanguageID)
        REFERENCES AvailableLanguage (LanguageID)
);

CREATE TABLE Attend (
    SystemID INT,
    SessionID INT,
    DateReg DATE,
    Satus VARCHAR(50),
    PRIMARY KEY (SystemID , SessionID),
    FOREIGN KEY (SystemID)
        REFERENCES Student (SystemID),
    FOREIGN KEY (SessionID)
        REFERENCES Session (SessionID)
);

CREATE VIEW SubjectTutorCount AS
    SELECT 
        s.SubjectID, s.SubjectName, COUNT(e.SystemID) AS TutorCount
    FROM
        SubjectsOfferfed AS s
            LEFT JOIN
        Expertise AS e ON s.subjectID = e.subjectID
    GROUP BY s.subjectID , s.SubjectName;

Insert into Person(SystemID, FirstName, LastName, Email, PhoneNum, UserName, Password)
Values
(1, 'James', 'Smith', 'jsmith@example.com', 5551011, 'jsmith', 'pass123'),
(2, 'Mary', 'Johnson', 'mjohnson@example.com', 5551012, 'mjohnson', 'pass456'),
(3, 'Michael', 'Jones', 'mjones@example.com', 5551013, 'mjones', 'pass789'),
(4, 'Patricia', 'Davis', 'pdavis@example.com', 5551014, 'pdavis', 'pass101'),
(5, 'John', 'Garcia', 'jgarcia@example.com', 5551015, 'jgarcia', 'pass111'),
(6, 'Jennifer', 'Anderson', 'janderson@example.com', 5551016, 'janderson', 'pass121'),
(7, 'Robert', 'Taylor', 'rtaylor@example.com', 5551017, 'rtaylor', 'pass131'),
(8, 'Linda', 'Wilson', 'lwilson@example.com', 5551018, 'lwilson', 'pass141'),
(9, 'David', 'Brown', 'dbrown@example.com', 5551019, 'dbrown', 'pass151'),
(10, 'Elizabeth', 'Jackson', 'ejackson@example.com', 5551020, 'ejackson', 'pass161'),
(11, 'William', 'Miller', 'wmiller@example.com', 5551021, 'wmiller', 'pass171'),
(12, 'Barbara', 'Martin', 'bmartin@example.com', 5551022, 'bmartin', 'pass181');
 
 Insert into Manager (SystemID, DateHired)
 Values
 (1, '2021-08-12'),
 (2, '2020-05-03'),
 (4, '2022-01-17');

Insert into Student (SystemID, PreferredLanguage)
Values
(3, 'English'), 
(5, 'Spanish'), 
(6, 'English'),
(7, 'French'), 
(8, 'English'), 
(9, 'Spanish'), 
(10, 'English'), 
(11, 'German'); 

Insert into Tutor (SystemID, DateHired, ManagedBy)
Values
(3,  '2022-08-01', 1),
(6,  '2023-01-15', 2),
(7,  '2021-11-10', 1),
(9,  '2022-03-22', 4),
(11, '2023-06-18', 2);

Insert into SubjectsOffered (SubjectID, SubjectName)
Values
(1, 'Math'),
(2, 'Biology'),
(3, 'Chemistry'),
(4, 'English Writing'),
(5, 'Computer Science');

Insert into Expertise ( SystemID, SubjectID, YearsOfExperience)
Values
(3, 1, 2),
(3, 4, 1),
(6, 5, 3),
(7, 1, 4),
(9, 2, 1),
(11, 4, 2),
(11, 5, 3);

Insert into AvailableLanguage( LanguageID, Language)
Values
(1, 'English'),
(2, 'Spanish'),
(3, 'French'),
(4, 'German');

Insert into Fluency (LanguageID, SystemID, Fluent)
Values
(1, 3, 'Yes'),
(1, 6, 'Yes'),
(3, 7, 'Yes'),
(2, 9, 'Yes'),
(1, 11, 'Yes'),
(4, 11, 'Yes');

Insert into Session (SessionID, SessionDate, SessionTime, Location, StudLim, SubjectID, SystemID, LanguageID)
Values
(1, '2024-11-01', '10:00:00', 'Room 101', 5, 1, 3, 1),
(2, '2024-11-02', '14:00:00', 'Online', 4, 5, 6, 1),
(3, '2024-11-03', '09:00:00', 'Room 303', 6, 2, 9, 2),
(4, '2024-11-04', '13:00:00', 'Online', 5, 4, 11, 1);

Insert into Attend ( SystemID, SessionID, DateReg, Satus) 
values
(5, 1, '2024-10-20', 'Registered'),
(8, 1, '2024-10-21', 'Attended'),
(10, 2, '2024-10-22', 'Attended'),
(9, 3, '2024-10-23', 'No-Show'),
(6, 4, '2024-10-25', 'Registered'),
(3, 4, '2024-10-26', 'No-Show');





