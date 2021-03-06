/****** Script for SelectTopNRows command from SSMS  ******/
SELECT distinct cardid,TRADETYPE,tradedate
  FROM [Metro].[dbo].[201906] where substring(tradedate,1,8)='2019/6/3' and TRADETYPE=50 order by cardid, tradedate

select count(*) from
(SELECT distinct cardid, count(TRADETYPE)%2 as c FROM [Metro].[dbo].[201906] where substring(tradedate,1,8)='2019/6/3' and TRADETYPE=50 group by cardid) as A
where c=1

select * from [Metro].[dbo].[201906] where cardid='6210813320014175606' and substring(tradedate,1,8)='2019/6/6'