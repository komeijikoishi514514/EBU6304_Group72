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

To build the project from source:  

```bash  
mvn clean package  
```  

After a successful build, the executable JAR file can be found in the `target` directory.
