# Introduction

This project involves documentation for PostgreSQL exercises covering fundamental topics such as Basics, Joins, Aggregation, and String Manipulation. These exercises are designed to ensure that developers can proficiently execute queries on relational databases, utilizing advanced features and formatting techniques.

# SQL Queries
## Table Setup (DDL)
The database is from a sports center management where diferrent infomation is being stored to help run the institution.

- `Members` This Table stores all the basic information of people that have used the facilities
- `Facilities` This Table lists all facilities made available to the Members
- `Bookings` refer to a reservation made by a member for a facility.

## Modifying data module
### Question 1: Insert a row
We used this query to setup a new facility `spa` to the table.
```sql
INSERT INTO cd.facilities 
VALUES 
  (9, 'Spa', 20, 30, 100000, 800);
```

### Question 2: Insert a row with auto-increment ID
Insert a row to table `cd.facilities` with auto-increment ID, to simplify the addition of new facilities for the future as workers wont have to manually increment the count
```sql
INSERT INTO cd.facilities (facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
VALUES (
               (SELECT COALESCE(MAX(facid), 0) + 1 FROM cd.facilities),
               'Spa', 20, 30, 100000, 800
       );
```

### Question 3: Update a value of a specific row
Used to update an existing facility.
```sql
UPDATE 
  cd.facilities 
SET 
  initialoutlay = 10000 
WHERE 
  name = 'Tennis Court 2';
```

### Question 4: Update a row based on other row(s)
Update a row from values of a different row. 
```sql
UPDATE cd.facilities
SET
    membercost = (SELECT membercost * 1.1 FROM cd.facilities WHERE name = 'Tennis Court 1'),
    guestcost = (SELECT guestcost * 1.1 FROM cd.facilities WHERE name = 'Tennis Court 1')
WHERE
    name = 'Tennis Court 2';
```

### Question 5: Delete an entire table
Delete an entire table.
```sql
DELETE FROM
  cd.bookings;
```

### Question 6: Delete a row
Used to delete a member from the database
```sql
Delete from cd.members where memid = 37;
```

## Basics Module
### Question 7: Select with conditions
Used to produce a list of facilities that charge a fee to members, and that fee is less than 1/50th of the monthly maintenance cost.
```sql
SELECT facid, name, membercost, monthlymaintenance
FROM cd.facilities
WHERE membercost>0 and membercost< monthlymaintenance / 50;;
```

### Question 8: Select with string condition
To display all facilities with the word Tennis in them
```sql
SELECT facid, name
FROM cd.facilities
WHERE name LIKE '%Tennis%';
```

### Question 9: Select with range
Display the facilities with ID 1 and 5
```sql
select *
from cd.facilities
where facid in (1,5);
```

### Question 10: Select with date
Select rows that is before a certain date. 
```sql
SELECT memid, surname, firstname, joindate
FROM cd.members
WHERE joindate > '2012-09-01';
```

### Question 11: Select two tables using union
Select two rows and place it to a single column. 
```sql
SELECT surname AS name
FROM cd.members
UNION
SELECT name
FROM cd.facilities;
```

## Join Module
### Question 12: Simple join
Used to find all booking made by member David Farrell.
```sql
SELECT b.starttime
FROM cd.bookings b
         JOIN cd.members m ON b.memid = m.memid
WHERE m.surname = 'Farrell' AND m.firstname = 'David';
```

### Question 13: Join between ordered dates
Used to create a list of the start times for bookings for tennis courts, for the date '2012-09-21
```sql
SELECT b.starttime, f.name
FROM cd.bookings b
         JOIN cd.facilities f ON b.facid = f.facid
WHERE f.name LIKE '%Tennis%'
  AND b.starttime >= '2012-09-21' AND b.starttime < '2012-09-22'
ORDER BY b.starttime;
```

### Question 14: Left outer join
Created a list of all members, including the individual who recommended them
```sql
SELECT m1.firstname AS memfname, m1.surname AS memsname,
       m2.firstname AS recfname, m2.surname AS recsname
FROM cd.members m1
         LEFT JOIN cd.members m2 ON m1.recommendedby = m2.memid
ORDER BY m1.surname, m1.firstname;
```

### Question 15: Inner distinct join
Join a table who have recommended another member. 
```sql
select distinct recs.firstname as firstname, recs.surname as surname
from
    cd.members mems
        inner join cd.members recs
                   on recs.memid = mems.recommendedby
order by surname, firstname;
```

### Question 16: Join without using join
Join tables without the keyword JOIN can be achieved using a subquery to list of all members, including the individual who recommended them
```sql
select distinct mems.firstname || ' ' ||  mems.surname as member,
                (select recs.firstname || ' ' || recs.surname as recommender
                 from cd.members recs
                 where recs.memid = mems.recommendedby
                )
from
    cd.members mems
order by member;
```

## Aggregation Module
### Question 17: Count aggregation
Count number of recurring value on a field. .
```sql
select recommendedby, count(*)
from cd.members
where recommendedby is not null
group by recommendedby
order by recommendedby;
```

### Question 18: Sum aggregation
Sum all values of a certain field. 
```sql
SELECT facid, sum(slots) AS slots
FROM cd.bookings
GROUP BY facid
ORDER BY facid;
```

### Question 19: Filter sum aggregation
Sum all values and filter by certain date. Note that you can sort the rows based on the aggregation value.
```sql
SELECT facid, sum(slots) AS slots
FROM cd.bookings
WHERE starttime >= '2012-09-01' AND starttime < '2012-10-01'
GROUP BY facid
ORDER BY sum(slots);
```

### Question 20: Extract date aggregation
Sum all values based on a specific date. 
```sql
SELECT facid,EXTRACT(MONTH FROM starttime) AS month,sum(slots) AS slots
FROM cd.bookings
WHERE EXTRACT(YEAR FROM starttime) = 2012
GROUP BY facid, EXTRACT(YEAR FROM starttime), EXTRACT(MONTH FROM starttime)
ORDER BY facid, month;
```

### Question 21: Subquery aggregation
Select unique rows for a field.
```sql
SELECT COUNT(DISTINCT memid) AS total_members
FROM cd.bookings;
```

### Question 22: Join aggregation
a list of each member name, id, and their first booking after September 1st 2012.
```sql
SELECT m.memid AS member_id,
       m.firstname AS member_firstname,
       m.surname AS member_surname,
       MIN(b.starttime) AS first_booking
FROM cd.members m
         LEFT JOIN cd.bookings b ON m.memid = b.memid
WHERE b.starttime >= '2012-09-01'
GROUP BY m.memid, m.firstname, m.surname
ORDER BY m.memid;
```

### Question 23: Window function
a list of member names, with each row containing the total member count.
```sql
SELECT m.surname AS member_surname,
       m.firstname AS member_firstname,
       COUNT(*) OVER () AS total_member_count
FROM cd.members m
ORDER BY m.joindate;
```

### Question 24: Row number window function
a monotonically increasing numbered list of members (including guests), ordered by their date of joining.
```sql
SELECT ROW_NUMBER() OVER (ORDER BY joindate) AS row_number,

        firstname,
       surname
FROM cd.members
ORDER BY joindate;
```

### Question 25: Subquery window function
Output the facility id that has the highest number of slots booked.
```sql
select facid, total from (
                             select facid, sum(slots) total, rank() over (order by sum(slots) desc) rank
                             from cd.bookings
                             group by facid
                         ) as ranked
where rank = 1
```

## String Module
### Question 26: Concat string
To concatenate a string, use the CONCAT keyword.
```sql
SELECT CONCAT(surname, ', ', firstname) AS member_name
FROM cd.members;
```

### Question 27: Regex string
You can find all the telephone numbers that contain parentheses, returning the member ID and telephone number sorted by member ID.
```sql
SELECT memid, telephone
FROM cd.members
WHERE telephone LIKE '%(%'
ORDER BY memid;
```

### Question 28: Substring
Used to produce a count of how many members you have whose surname starts with each letter of the alphabet.
```sql
SELECT LEFT(surname, 1) AS initial_letter, COUNT(*) AS member_count
FROM cd.members
GROUP BY LEFT(surname, 1)
HAVING COUNT(*) > 0
ORDER BY initial_letter;
```



