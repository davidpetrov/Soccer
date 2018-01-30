

SELECT DISTINCT a.hometeamname, b.awayteamname
FROM fixtures a
CROSS JOIN fixtures b
where a.competition='SPA' and a.startyear=2017 and b.competition='SPA' and b.startyear=2017 and a.hometeamname<>b.awayteamname

EXCEPT

SELECT DISTINCT hometeamname, awayteamname
FROM fixtures a
WHERE a.competition='SPA' and a.startyear=2017

