import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.json.JSONArray;

public class Start {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        
        // Define variables
        LocalDate startDate = null;
        LocalDate endDate = null;
        int totalDay = 0;
        int totalPairs = 0;
        List<String> currencyPairs = new ArrayList<>();
        List<String> overallCurrencyData = new ArrayList<>();
        
        // Gathering inputs
        boolean inputFlag = true;
        while (inputFlag == true) {
            
            // clear data
            startDate = null;
            endDate = null;
            totalDay = 0;
            totalPairs = 0;
            currencyPairs.clear();
            overallCurrencyData.clear();      
        
            // input data
            List<Object> inputResult = inputOverallData(scanner); // Calling input function
            startDate = (LocalDate) inputResult.get(0);
            endDate = (LocalDate) inputResult.get(1);
            totalDay = (int) inputResult.get(2);
            totalPairs = (int) inputResult.get(3);
            currencyPairs = (List<String>) inputResult.get(4);

            // Connect Coinbase API to fetchingData a data
            String errMessage = "An error occurred while fetching data. \n"
                        + "Data may not be available within this date range, or the currency pair symbol could be incorrect. \n"
                        + "Please try again. \n"
                        + "==================================================================================";  
            try {
                overallCurrencyData = FetchingData(startDate, endDate, totalPairs, currencyPairs);
                // insert loop to check all assets
                boolean checkEmpty = false;
                for (int i = 0; i < overallCurrencyData.size(); i++) {
                    // overallCurrencyData.get(0) has a type of java.lang.String 
                    if (overallCurrencyData.get(i).equals("[]")) checkEmpty = true;
                }
                if (checkEmpty == true) {
                    System.out.println(errMessage);  
                } else {
                    inputFlag = false;
                }
            } catch (Exception e) {
                System.out.println(errMessage);  
            }            
        }
        
        // Convert to JSON Array type 
        JSONArray overallCurrency = new JSONArray();
        for (int j = 0; j < totalPairs; j++) {
            JSONArray individualCurrency = new JSONArray(overallCurrencyData.get(j));
            overallCurrency.put(individualCurrency);
        }      
          
        // Create daily return in JSON Object format
        JSONArray overallDailyReturn = new JSONArray();
        for (int i = 0; i < totalPairs; i++) {
            JSONArray currency = CalculateDailyReturn(overallCurrency.getJSONArray(i));
            overallDailyReturn.put(currency);
        }
        
        // Calculate indivitual statisic data
        statisticCalculation dataCalculation = new statisticCalculation();
        JSONArray overallStatistic = new JSONArray();
        for (int i = 0; i < totalPairs; i++) {
            JSONArray individualStatistic = new JSONArray();
            individualStatistic.put(currencyPairs.get(i)); // Name seq. 0
            // Mean calculation seq. 1
            double mean = statisticCalculation.Mean(overallDailyReturn.getJSONArray(i));
            individualStatistic.put(mean);
            // Variance calculation seq. 2
            double variance = statisticCalculation.Variance(overallDailyReturn.getJSONArray(i), mean);
            individualStatistic.put(variance);
            // Standard Deviation calculation seq. 3
            double sd = Math.sqrt(variance);
            individualStatistic.put(sd); 
            // Return over the period seq. 4
            double returnOverPeriod = statisticCalculation.returnOverPeriod(overallCurrency.getJSONArray(i));
            individualStatistic.put(returnOverPeriod); // seq. 4
            // Store all indivitual statisic data
            overallStatistic.put(individualStatistic);
        }
        
        // Find total Covariance pairs
        JSONArray totalCovariancePair = new JSONArray();
        totalCovariancePair = statisticCalculation.totalCovariancePair(totalPairs);
        
        // Find all Covariance
        JSONArray allCovariance = new JSONArray();
        for (int i = 0; i < totalCovariancePair.length(); i ++) {
            int first = totalCovariancePair.getJSONArray(i).getInt(0);
            int second = totalCovariancePair.getJSONArray(i).getInt(1);
            double meanFirst = overallStatistic.getJSONArray(first).getDouble(1);
            double meanSecond = overallStatistic.getJSONArray(second).getDouble(1);
            double indCov = statisticCalculation.Covariance(overallDailyReturn.getJSONArray(first), meanFirst, overallDailyReturn.getJSONArray(second), meanSecond);
            
            // add position with covariance
            JSONArray indCovariance = new JSONArray();
            String pairName = (String)currencyPairs.get(first) + ":" + (String)currencyPairs.get(second);
            indCovariance.put(pairName); // seq. 0 pair name
            indCovariance.put(first); // seq. 1 first pair
            indCovariance.put(second); // seq. 2 second pair
            indCovariance.put(indCov); // seq. 3 covariance
            
            // store all
            allCovariance.put(indCovariance);
        }
        
        // Adjusting asset allocation
        boolean changeProportionFlag = true;
        while (changeProportionFlag == true) {
            // Proportion of each assets
            JSONArray overallProportion = allProportion(scanner, overallStatistic);      

            // Find variance of portfolio
            double portfolioVariace = statisticCalculation.portfolioVariance(overallProportion, overallStatistic, allCovariance) * 100;

            // Summary Paper
            SummaryPaper(overallStatistic, overallProportion, portfolioVariace, startDate, endDate, totalDay);

            // try more!!
            for (int i = 0; i < 3; i++) System.out.println(".");
            System.out.println("======================================================================== ");
            System.out.println("""
                               Would you like to explore additional diversification strategies by adjusting asset allocation proportions? 
                               If yes, pleas enter 'Y'. Otherwise, simply press any key.""");
            scanner.nextLine();
            String chooseMore = scanner.nextLine();
            if (chooseMore.equalsIgnoreCase("Y")) {
                System.out.println("Let's go!!");
                for (int i = 0; i < 3; i++) System.out.println("V");
            } else {
                System.out.println("Have a good day!!");
                changeProportionFlag = false;
            }
        }  
    }
    
    // =========================================================================
        // User input data function
        private static List<Object> inputOverallData(Scanner scanner) {
            List<Object> inputResult = new ArrayList<>();

            // Input start and end dates
            System.out.println("Please insert start and end of the period "
                    + "by using this date format (YYYY-MM-DD).\n"
                    + "The calculation has been limited below 300 days.");
            LocalDate startDate = null;
            LocalDate endDate = null;
            boolean checkDateFlag = true;
            long finalDateRange = 0;
            while (checkDateFlag == true) {
                System.out.print("Start date: ");
                String startDateString = scanner.nextLine();
                System.out.print("End date: ");
                String endDateString = scanner.nextLine();

                // Convert to LocalDate format
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                // Check date format and total date range below 300 days
                try {
                    startDate = LocalDate.parse(startDateString, formatter);
                    endDate = LocalDate.parse(endDateString, formatter);
                    long dateRange = endDate.toEpochDay() - startDate.toEpochDay() + 1;
                    if (dateRange <= 0) {
                        System.out.println("The end date need to come after the start date. Please try again.");
                    }
                    if (dateRange > 0 && dateRange <= 300) {
                        System.out.println("Total number of days: " + dateRange);
                        finalDateRange = dateRange;
                        checkDateFlag = false;
                    }
                    if (dateRange > 300) {
                        System.out.println("Total number of days: " + dateRange);
                        System.out.println("Total day exceed the limit. Please try again.");
                    }
                } catch (DateTimeParseException e) {
                    System.out.println("Wrong date format. Please try again.");
                }           
            }
            inputResult.add(startDate);
            inputResult.add(endDate);
            inputResult.add((int) finalDateRange);

            // Input total calculated pair
            System.out.print("Please insert total number of currency pair in your portfolio. ");
            System.out.println("Minimum of 2 and Maximum of 10 pairs.");
            int totalPairs = 0;
            boolean checkPairNumberFlag = true;
            while (checkPairNumberFlag == true) {
                System.out.print("Total number of currency pair(s): ");
                try {
                    totalPairs = scanner.nextInt();
                    if (totalPairs <= 10 && totalPairs >= 2) {
                        checkPairNumberFlag = false;
                    } else {
                        System.out.println("Total pair below or exceed the limit. Please try again.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("You must input the number. Please try again.");
                    scanner.nextLine();
                }            
            }       
            inputResult.add(totalPairs);
            scanner.nextLine();
                
            // Input selected Pair
            System.out.println("Please insert currency pair name of selected " 
                    + totalPairs + " pairs in this format e.g., BTC-USD, ETH-USD.");
            List<String> currencyPairs = new ArrayList<>();

            for (int n = 1; n <= totalPairs; n++) {
                System.out.print(n + ". currency pair name: ");
                String currencyName = scanner.nextLine();
                currencyPairs.add(currencyName);
            }
            inputResult.add(currencyPairs);
            return inputResult;
        }
    // =========================================================================
        // Retrived data from coinbase function
        private static List<String> FetchingData(LocalDate startDate, LocalDate endDate, int totalPairs, List<String> currencyPairs) throws Exception {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            List<String> OverallData = new ArrayList<>();
            for (int i = 0; i < totalPairs; i++) {
                // Construct an URL
                String apiService = "https://api.exchange.coinbase.com" +
                                    "/products/" + currencyPairs.get(i) + "/candles?" +
                                    "granularity=86400&" +
                                    "start=" + startDate.format(formatter) + "&" +
                                    "end=" + endDate.format(formatter);
                
                // Connect a Coinbase API
                URI uri = URI.create(apiService);
                URL url = uri.toURL();
                InputStream input = url.openStream();
                
                // Read the response as a string
                StringBuilder responseBuilder = new StringBuilder();
                while (true) {
                    int k = input.read();
                    if (k == -1) break;
                    responseBuilder.append((char) k);
                }
                String response = responseBuilder.toString();
                OverallData.add(response);
            }
            return OverallData;
        }
    // =========================================================================
        // Calculate daily return function (input json array return json array)
        private static JSONArray CalculateDailyReturn(JSONArray currencyData) {
            JSONArray indDailyReturn = new JSONArray();
            // Calculate daily return
            for (int j = currencyData.length() - 1; j > 0; j--) {
                // input data as a format [timestamp, price_low, price_high, price_open, price_close]
                double currentPrice = currencyData.getJSONArray(j).getDouble(4); // retrieve closed price
                double previousPrice = currencyData.getJSONArray(j - 1).getDouble(4);
                double DailyReturn = ((currentPrice - previousPrice) / previousPrice);
                indDailyReturn.put(DailyReturn);
            }
            //
            return indDailyReturn;
        } 
    // =========================================================================
        // Find expected daily return (Unused)
        private static JSONArray getDailyReturn(Scanner scanner, JSONArray overallStatistic) {
            JSONArray expectedDailyReturn = new JSONArray();
            
            // Choose manually or use historical-based return
            System.out.println("Insert the expected daily return. But If you don't want to insert it manually,\n"
                    + "you can use a return based on historical daily return that we provided \n"
                    + "(Using mean daily return which calculated from closed price over the selected period). \n"
                    + "Too choose manually, please press 'Y'. Or use historical daily return, please press 'N'.");

            boolean dailyReturnFlag = true;
            while (dailyReturnFlag == true) {
                System.out.print("Manually choose expected daily return: ");
                String choose = scanner.nextLine();                
                try {
                    if (choose.equals("Y")) {
                        System.out.println("Insert expected daily return of each coin in percent(%).");
                        for (int i = 0; i < overallStatistic.length(); i++) {
                            String name = overallStatistic.getJSONArray(i).getString(0);
                            System.out.print("Expected daily return for " + name + ": ");
                            double manualReturn = scanner.nextDouble();
                            expectedDailyReturn.put(manualReturn);
                        }                    
                        dailyReturnFlag = false;
                    }
                    if (choose.equals("N")) {
                        for (int i = 0; i < overallStatistic.length(); i++) {
                            expectedDailyReturn.put(overallStatistic.getJSONArray(i).getDouble(1));
                        }
                        dailyReturnFlag = false;
                    } else {
                        if (dailyReturnFlag == true) {
                            System.out.println("You must input Y/N. Please try again.");
                        } 
                    }                    
                } catch (InputMismatchException e) {
                    System.out.println("You must input Y/N. Please try again.");
                }         
            }
            return expectedDailyReturn;
        }
    // =========================================================================
        // Find proportion of each asset in portfolio      
        private static JSONArray allProportion(Scanner scanner, JSONArray overallStatistic) {
            System.out.println("Insert proportion of each coins in portfolio(%). "
                    + "Total proportion must equal 100%.");
            JSONArray overallProportion = new JSONArray();
            
            boolean proportionFlag = true;
            while (proportionFlag == true) {
                double total = 0;
                for (int i = 0; i < overallStatistic.length(); i++) {
                    boolean validInputFlag = true;
                    while (validInputFlag == true) {
                        try {
                            String name = overallStatistic.getJSONArray(i).getString(0);
                            System.out.print("proportion of " + name + " in portfolio: ");
                            double proportion = scanner.nextDouble();
                            total += proportion;
                            overallProportion.put(proportion);
                            validInputFlag = false;
                        } catch (InputMismatchException e) {
                            System.out.println("Invalid input format. Please enter a valid number.");
                            scanner.next();
                        }                        
                    }
                }
                if (total == 100) {
                    proportionFlag = false;
                } else {
                    System.out.println("Total proportion is " + total + "% and not equal 100%. Please try again.");
                    overallProportion.clear();
                }
            }
            return overallProportion;
        }
    // =========================================================================
        // Summary of overall calculation   
        private static void SummaryPaper(JSONArray overallStatistic,JSONArray overallProportion,double portfolioVariace,
                LocalDate startDate, LocalDate endDate, int totalDay) {

            for (int i = 0; i < 3; i++) {
                System.out.println(".");
            }
            
            System.out.println("======================================================================== \n"
                    + "The results of the Cryptocurrency Portfolio Analysis Version 0.1 calculation \n"
                    + "Start Date: " + startDate + " End Date: " + endDate + " \n"
                    + "Total days: " + totalDay + " \n"
                    + "========================================================================  \n"
                    + "Individual statistics for each coin over the specified period \n"
                    + "------------------------------------------------------------------------ ");
            JSONArray overallPortReturn = new JSONArray();
            for (int i = 0; i < overallStatistic.length(); i++) {
                String name = overallStatistic.getJSONArray(i).getString(0); // Name
                double mean = overallStatistic.getJSONArray(i).getDouble(1) * 100; // Mean
                double sd = overallStatistic.getJSONArray(i).getDouble(3) * 100; // S.D.
                double percentReturnOverPeriod = overallStatistic.getJSONArray(i).getDouble(4) * 100; // return over period
                overallPortReturn.put(percentReturnOverPeriod);
                System.out.printf("%s have a return of %.2f%% percent over the period"
                        + ", mean daily return at %.2f%%, and s.d. of daily return at %.3f%% \n", name, percentReturnOverPeriod,  mean, sd);
            }
            
            // Find holding seperately
            double seperatedHoldVariance = 0;
            for (int i = 0; i < overallStatistic.length(); i++) {
                double individualSD = overallStatistic.getJSONArray(i).getDouble(3) * 100; // s.d.
                double individualProportion = overallProportion.getDouble(i) / 100;
                seperatedHoldVariance += individualSD * individualProportion; // s.d.
            }
            
            System.out.println("======================================================================== \n"
                    + "Portfolio statistics over the specified period \n"
                    + "------------------------------------------------------------------------ ");
            double portReturn = 0;
            for (int i = 0; i < overallStatistic.length(); i++) {
                double proportion = overallProportion.getDouble(i) / 100;
                double returnOverPeriod = overallPortReturn.getDouble(i);
                portReturn += proportion * returnOverPeriod;
            }
            System.out.println("For diversification strategy which holding proportion as followed: ");
            for (int i = 0; i < overallStatistic.length(); i++) {
                String name = overallStatistic.getJSONArray(i).getString(0); // Name
                double proportion = overallProportion.getDouble(i);
                System.out.printf("%s for %.2f%% \n", name, proportion);
            }
            System.out.printf("Generate portfolio return of %.2f%% with s.d. of daily return portfolio at %.3f%% \n",portReturn, portfolioVariace);
            
            // summary
            System.out.println("------------------------------------------------------------------------ ");
            if (portfolioVariace <= seperatedHoldVariance) {
                System.out.printf("The volatility of your portfolio has decreased!!%n"
                        + "Variation of daily return by holding each asset individually has decreased from %.3f%% to %.3f%% with your diversification strategy.%n",
                        seperatedHoldVariance, portfolioVariace);
            } else {
                System.out.printf("This diversification strategy may not work well.%n"
                        + "Since, the volatility of daily return by holding each asset individually has increased from %.3f%% to %.3f%% with your diversification strategy.%n"
                        + "Better choose a different proportion for your assets or hold each asset separately.%n",
                        seperatedHoldVariance, portfolioVariace);            
            } 
        }   
}

class statisticCalculation {
    // Mean method Calculation
    public static double Mean(JSONArray currencyReturnData) {
        double sum = 0;
        for (int i = 0; i < currencyReturnData.length(); i++) {
            sum += currencyReturnData.getDouble(i);
        }
        double meanOfreturn = sum / currencyReturnData.length();
        return meanOfreturn;
    }
    
    // Variance method Calculation
    public static double Variance(JSONArray currencyReturnData, double mean) {
        double sumSquaredDifference = 0;
        for (int i = 0; i < currencyReturnData.length(); i++) {
            sumSquaredDifference += Math.pow(currencyReturnData.getDouble(i) - mean, 2);
        }
        double varianceOfReturns = sumSquaredDifference / (currencyReturnData.length() - 1);
        return varianceOfReturns;
    }
    
    // Covariance method Calculation
    public static double Covariance(JSONArray dataX, double meanX, JSONArray dataY, double meanY) {
        double covariance = 0;
        for (int i = 0; i < dataX.length(); i++) {
            covariance += (dataX.getDouble(i) - meanX) * (dataY.getDouble(i) - meanY);
        }
        covariance = covariance/(dataX.length() - 1);
        return covariance;
    }
    
    // Find total possible pair of covariance
    public static JSONArray totalCovariancePair(double totalPairs) {
        JSONArray covariancePair = new JSONArray();
        for (int i = 0; i < totalPairs; i++) {
            for (int j = i + 1; j < totalPairs; j++) {
                JSONArray pair = new JSONArray();
                pair.put(i);
                pair.put(j);
                covariancePair.put(pair);
            }
        }
        return covariancePair;
    }
    
    // Variance of portfolio
    public static double portfolioVariance(JSONArray proportion, JSONArray overallStatistic, JSONArray allCovariance) {
        // Calculate first group - sum of (W(n)^2 * variance)
        double firstTermGroup = 0;
        for (int i = 0; i < proportion.length(); i++) {
            double percent = proportion.getDouble(i) / 100;
            double variance = overallStatistic.getJSONArray(i).getDouble(2); // variance
            firstTermGroup += percent*percent*variance;
        }
        // Calculate second group - sum of (2 * W(n1) * W(n2) * Cov)
        double secondTermGroup = 0;
        for (int j = 0; j < allCovariance.length(); j++) {
            int indexFirst = allCovariance.getJSONArray(j).getInt(1); // First index
            int indexSecond = allCovariance.getJSONArray(j).getInt(2); // Second index
            double cov = allCovariance.getJSONArray(j).getInt(3); // Covariance
            
            double proportionFirst = proportion.getDouble(indexFirst) / 100;
            double proportionSecond = proportion.getDouble(indexSecond) / 100;
            
            secondTermGroup += proportionFirst*proportionSecond*cov;
        }
        double totalPortVariance = firstTermGroup + secondTermGroup;
        totalPortVariance = Math.sqrt(totalPortVariance);
        return totalPortVariance;
    }
    
    // Return over the selected period
    public static double returnOverPeriod(JSONArray CurrencyData) {
        double LastClosedPrice = CurrencyData.getJSONArray(0).getDouble(4);
        double firstClosedPrice = CurrencyData.getJSONArray(CurrencyData.length()-1).getDouble(4);
        double returnOverPeriod = (LastClosedPrice - firstClosedPrice) / firstClosedPrice;
        return returnOverPeriod;
    }      
}