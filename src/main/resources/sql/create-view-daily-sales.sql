CREATE VIEW daily_sales AS
SELECT CAST(date_of_sale AS DATE) AS DATE, SUM(sale_price) as total_sales
FROM vanguard.game_sales
group by date;