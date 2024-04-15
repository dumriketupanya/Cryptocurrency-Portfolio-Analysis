# Cryptocurrency-Portfolio-Analysis

## Getting Started

To begin using the program, follow these steps after running it.

### Inputs

<p align="center">
  <img src="https://github.com/dumriketupanya/Cryptocurrency-Portfolio-Analysis/raw/main/Pictures/Input.png" width="822" height="360" />
</p>

**Initial Input:**
- ***Start and End dates (number 1):***  Enter the start and end dates for the analysis period by using the following date format (YYYY-MM-DD). Please note that the calculation is limited to below 300 days due to Coinbase API fetching limitations.
   - If an error occurs, data fetching from Coinbase may not be available within this date range, or the currency pair symbol could be incorrect.

- ***total number of currency pairs (number 2):*** Specify the total number of currency pairs to be included in your portfolio. Minimum of 2 and Maximum of 10 pairs.
- ***currency pairs name (number 3):*** Provide the names of the selected currency pairs in the format e.g., BTC-USD, ADA-USD.
    - We recommend using the same base currency (in this case, USD) to ensure the accuracy of calculations.

**Portfolio Proportions:**
- ***Proportion of each coin (number 4):*** Define the allocation percentages for each currency pair within the portfolio. Ensure that the total sum of proportions equals 100%.


### Calculation Results
If the calculation is successful, the result will be displayed as follows.

<p align="center">
  <img src="https://github.com/dumriketupanya/Cryptocurrency-Portfolio-Analysis/blob/main/Pictures/Result.png" width="1073" height="447" />
</p>

The description of Results:
- ***Initial Input (number 1):*** Start date, End date, and total length of the calculation period.

- ***Return of individual coin over the selected period (number 2):*** The gain/loss of each coin since the start date through the end date.

- ***Mean daily return (number 3):*** The average gain/loss of each coin between each day during the selected period.

- ***Standard deviation (s.d.) of daily return (number 4):*** The standard deviation of the gain/loss of each coin between each day during the selected period. This metric reflects the fluctuation of each coin.
    - The closed price is utilized for the calculation in each result.
####

```
  BTC-USD have a return of 41.19% percent over the period, mean daily return at -0.54%, and s.d. of daily return at 2.680%
```
- ***Portfolio allocation proportions (number 5):*** Allocation percentages for each currency pair within the portfolio as defined in the inputs.

- ***Overall portfolio (number 6):*** The gain/loss of the portfolio with the selected proportions. Return calculated from the start date through the end date.

- ***Standard deviation of daily return (number 7):*** The standard deviation of the gain/loss in the portfolio between each day during the selected period. This metric is crucial for optimization.
####

```
  Generate portfolio return of 38.00% with s.d. of daily return portfolio at 1.715% 
```

- ***The result (number 8):*** a decrease in the standard deviation indicates improved stability.
####

```
The volatility of your portfolio has decreased!!
Variation of daily return by holding each asset individually has decreased from 2.938% to 1.715% with your diversification strategy.
```

### What's next?

Once the calculation process is complete, you can enter 'Y' to try other proportions again. This allows you to compare different allocation strategies and find the most suitable one for your portfolio.

<p align="center">
  <img src="https://github.com/dumriketupanya/Cryptocurrency-Portfolio-Analysis/blob/main/Pictures/Rerun.png" width="1078" height="730" />
</p>
If you have any further questions or encounter any errors, calculation mistakes, or if you would like to suggest improvements to enhance the program. Please feel free to contact me via GitHub or email.

Thank you very much! Enjoy and have a great investment journey.

