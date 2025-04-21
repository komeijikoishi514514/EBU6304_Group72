# EBU6304_Group72
# Personal Wealth Assistant  

Personal Wealth Assistant is a comprehensive desktop application for personal financial management, designed to help users track income and expenses, set budgets, and monitor financial health.  

## Key Features  

1. **Dashboard** - Provides an overview of financial status, including income, expenses, and budget utilization  
2. **Manual Entry** - Easily add and edit transaction records  
3. **Import Transactions** - Bulk import transaction data from CSV or JSON files  
4. **Transaction History** - Browse, search, and manage historical transaction records  
5. **Reports** - Generate visual charts and reports to understand financial trends  
6. **Settings** - Customize categories, budget limits, and application preferences  

## Technical Features  

- Cross-platform desktop application developed in Java  
- User interface built with the Swing framework  
- Data stored locally in JSON or CSV format  
- No internet connection required, ensuring privacy  
- Flexible and extensible architecture  

## System Requirements  

- Java 17 or later  
- Minimum screen resolution: 1024 x 768  
- Operating System: Windows, macOS, or Linux  

## Usage Instructions  

1. Ensure Java 17 or later is installed on your system  
2. Double-click `wealth5/target/wealth-assistant-1.0-SNAPSHOT-jar-with-dependencies.jar` to launch the application  
3. On first use, the program will automatically create necessary data directories and default settings  
4. Customize categories and budgets in the "Settings" panel  
5. Start recording and managing your financial information  

## Building the Project  

To build the project, please run the following command in the wealth5 directory:  

```bash  
mvn clean package  
```  

After a successful build, the executable JAR file can be found in the `target` directory.

## AI Component Description

The current prototype and project documentation do not explicitly outline AI-specific implementations. However, potential AI applications can be inferred from the described features and feedback, particularly in enhancing the Reports and Transaction History functionalities:  
1. Trend Forecasting & Spending Insight
   - The prototype mentions "trend forecasts" and "spending insights" on the Reports page.  
   - AI Opportunity: A machine learning model (e.g., time-series forecasting) could analyze historical transaction data to predict future spending trends. This would automate the generation of actionable insights (e.g., budget warnings, seasonal expense patterns).  
2. Automated Transaction Categorization
   - The "Manual Entry" page includes a category dropdown (e.g., Food, Transport).  
   - AI Opportunity: Natural Language Processing (NLP) could analyze transaction notes or descriptions to auto-categorize entries, reducing manual input errors and improving efficiency.  
3. Anomaly Detection
   - Transaction History allows filtering by date and category.  
   - AI Opportunity: An anomaly detection algorithm could flag unusual spending patterns (e.g., sudden large expenses) in real-time, enhancing financial oversight.  
4. Data Parsing & Import Optimization
   - The "Import Transactions" page supports CSV/JSON parsing.  
   - AI Opportunity: AI-driven data validation could improve error detection during file imports (e.g., mismatched formats, outlier detection).  
Implementation Considerations
- The project’s Java codebase (noted in the marksheet) could integrate libraries like TensorFlow or scikit-learn for model deployment.  
- Testing feedback highlights the need for detailed documentation and JUnit examples, which would be critical for validating AI components (e.g., model accuracy, edge cases).  
- The prototype’s low score (2/4) suggests prioritizing core functionalities first, with AI enhancements proposed as future iterations.  
Conclusion
While AI is not explicitly implemented in the current version, the system’s design provides foundational data structures (transaction records, categories) and use cases (forecasting, categorization) that align well with AI integration for advanced automation and analytics.
