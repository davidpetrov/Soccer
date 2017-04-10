SELECT distinct date,hometeamname,COUNT(DISTINCT AWAYTEAMNAME) as counts
FROM PLAYERFIXTURES
WHERE COMPETITION='IT' and year=2012
GROUP BY HOMETEAMNAME
order by counts asc

SELECT distinct date,awayteamname,COUNT(DISTINCT homeTEAMNAME) as counts
FROM PLAYERFIXTURES
WHERE COMPETITION='IT' and year=2012
GROUP BY awayteamname
order by counts asc

INSERT INTO PLAYERFIXTURES (DATE,HOMETEAMNAME,AWAYTEAMNAME,HOMEGOALS,AWAYGOALS,YEAR,COMPETITION,TEAM,NAME,MINUTESPLAYED,LINEUP,SUBSTITUTE,GOALS,ASSISTS)VALUES ('2013-05-26T00:00:00Z','Athletic Club','Levante',0,1,2012,'SPA','Athletic Club','Ibai Gmez',35,0,1,0,0 );
select  * from playerfixtures
where date = '2013-05-26T00:00:00Z' and hometeamname='Athletic Club' and awayteamname='Levante' and team = 'Athletic Club' and name='Ibai Gmez'

select date,hometeamname,awayteamname,team,name
from playerfixtures
group by date,hometeamname,awayteamname,team,name
having count() > 1;

select count(*)
from playerfixtures

delete 
from playerfixtures
where COMPETITION='GER' and year=2011