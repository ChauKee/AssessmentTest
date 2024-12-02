CREATE VIEW daily_game_sales AS
SELECT CAST(date_of_sale AS DATE) AS DATE, game_no, SUM(sale_price) as total_sales
FROM vanguard.game_sales
group by date, game_no;
