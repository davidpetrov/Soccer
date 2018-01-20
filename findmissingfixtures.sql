

SELECT DISTINCT a.hometeamname, b.awayteamname
FROM fixtures a
CROSS JOIN fixtures b
where a.competition='ENG2' and a.startyear=2010 and b.competition='ENG2' and b.startyear=2010 and a.hometeamname<>b.awayteamname

EXCEPT

SELECT DISTINCT hometeamname, awayteamname
FROM fixtures a
WHERE a.competition='ENG2' and a.startyear=2010

