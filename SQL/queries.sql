'Question 1'
INSERT INTO cd.facilities
VALUES
  (9, 'Spa', 20, 30, 100000, 800);

'Question 2'
INSERT INTO cd.facilities (facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
VALUES (
  (SELECT COALESCE(MAX(facid), 0) + 1 FROM cd.facilities),
  'Spa', 20, 30, 100000, 800
);

'Question 3'
UPDATE
    cd.facilities
SET
    initialoutlay = 10000
WHERE
    name = 'Tennis Court 2';

'Question 4'
UPDATE cd.facilities
SET
    membercost = (SELECT membercost * 1.1 FROM cd.facilities WHERE name = 'Tennis Court 1'),
    guestcost = (SELECT guestcost * 1.1 FROM cd.facilities WHERE name = 'Tennis Court 1')
WHERE
    name = 'Tennis Court 2';

'Question 5'
DELETE FROM cd.bookings;

'Question 6'
delete from cd.members where memid = 37;

'Question 7'
SELECT facid, name, membercost, monthlymaintenance
FROM cd.facilities
WHERE membercost>0 and membercost< monthlymaintenance / 50;

'Question 8'
SELECT facid, name
FROM cd.facilities
WHERE name LIKE '%Tennis%';

'Question 9'
select *
from cd.facilities
where facid in (1,5);

'Question 10'
SELECT memid, surname, firstname, joindate
FROM cd.members
WHERE joindate > '2012-09-01';

'Question 11'
SELECT surname AS name
FROM cd.members
UNION
SELECT name
FROM cd.facilities;

'Question 12'
SELECT b.starttime
FROM cd.bookings b
         JOIN cd.members m ON b.memid = m.memid
WHERE m.surname = 'Farrell' AND m.firstname = 'David';

'Question 13'
SELECT b.starttime, f.name
FROM cd.bookings b
         JOIN cd.facilities f ON b.facid = f.facid
WHERE f.name LIKE '%Tennis%'
  AND b.starttime >= '2012-09-21' AND b.starttime < '2012-09-22'
ORDER BY b.starttime;

'Question 14'
SELECT m1.firstname AS memfname, m1.surname AS memsname,
       m2.firstname AS recfname, m2.surname AS recsname
FROM cd.members m1
         LEFT JOIN cd.members m2 ON m1.recommendedby = m2.memid
ORDER BY m1.surname, m1.firstname;

'Question 15'
select distinct recs.firstname as firstname, recs.surname as surname
from
    cd.members mems
        inner join cd.members recs
                   on recs.memid = mems.recommendedby
order by surname, firstname;

'Question 16'
select distinct mems.firstname || ' ' ||  mems.surname as member,
                (select recs.firstname || ' ' || recs.surname as recommender
                 from cd.members recs
                 where recs.memid = mems.recommendedby
                )
from
    cd.members mems
order by member;

'Question 17'
select recommendedby, count(*)
from cd.members
where recommendedby is not null
group by recommendedby
order by recommendedby;

'Question 18'
SELECT facid, sum(slots) AS slots
FROM cd.bookings
GROUP BY facid
ORDER BY facid;

'Question 19'
SELECT facid, sum(slots) AS slots
FROM cd.bookings
WHERE starttime >= '2012-09-01' AND starttime < '2012-10-01'
GROUP BY facid
ORDER BY sum(slots);

'Question 20'
SELECT facid,EXTRACT(MONTH FROM starttime) AS month,sum(slots) AS slots
FROM cd.bookings
WHERE EXTRACT(YEAR FROM starttime) = 2012
GROUP BY facid, EXTRACT(YEAR FROM starttime), EXTRACT(MONTH FROM starttime)
ORDER BY facid, month;

'Question 21'
SELECT COUNT(DISTINCT memid) AS total_members
FROM cd.bookings;

'Question 22'
SELECT m.memid AS member_id,
       m.firstname AS member_firstname,
       m.surname AS member_surname,
       MIN(b.starttime) AS first_booking
FROM cd.members m
         LEFT JOIN cd.bookings b ON m.memid = b.memid
WHERE b.starttime >= '2012-09-01'
GROUP BY m.memid, m.firstname, m.surname
ORDER BY m.memid;

'Question 23'
SELECT m.surname AS member_surname,
       m.firstname AS member_firstname,
       COUNT(*) OVER () AS total_member_count
FROM cd.members m
ORDER BY m.joindate;

'Question 24'
SELECT ROW_NUMBER() OVER (ORDER BY joindate) AS row_number,

        firstname,
       surname
FROM cd.members
ORDER BY joindate;

'Question 25'
select facid, total from (
                             select facid, sum(slots) total, rank() over (order by sum(slots) desc) rank
                             from cd.bookings
                             group by facid
                         ) as ranked
where rank = 1

    'Question 26'
SELECT CONCAT(surname, ', ', firstname) AS member_name
FROM cd.members;

'Question 27'
SELECT memid, telephone
FROM cd.members
WHERE telephone LIKE '%(%'
ORDER BY memid;

'Question 28'
SELECT LEFT(surname, 1) AS initial_letter, COUNT(*) AS member_count
FROM cd.members
GROUP BY LEFT(surname, 1)
HAVING COUNT(*) > 0
ORDER BY initial_letter;
