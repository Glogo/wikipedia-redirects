#Server
Java project responsible for presenting saved Wikipedia alternative titles on webpage.

## Eclipse project setup
You need to use Eclipse EE for this project and have Tomcat server installed

1. Import project as Java or Maven Project
2. Open Run Configurations and add following VM Argument `-Xmx6g`
3. Click apply and close dialog
4. Right click on your project in project browser and select Run As -> Run on Server
5. Eclipse should ask you to create new server or to add this project to existing server. Follow Eclipse instructions
6. Open your server configuration (separate Eclipse project named `Servers` should be automatically created) and edit `server.xml`. Find `Context` element containing attribute `path="/Wikipedia Redirects Server"` and change it to something simpler f.e. `"/redirects"`
7. Open http://localhost:8080/redirects webpage. At first let server read input CSV file and initialize Lucene index (see Eclipse console)
8. Enter your search term and profit

##Notes
- Server project already contains parsed complete wikipedia dump in file `resources/data.csv.zip`
- Upon server startup try calling following URL http://localhost:8080/redirects/webapi/alt/search/Parser you will have direct REST API access to server resources. In link above we are searching for term `"Parser"` and server should output something like:
```json
{
  "LR parser": [
    "LR(0) parser",
    "LR parsers",
    "Lr parser",
    "LR(0)",
    "LR parsing",
    "LR Parser",
    "Shift-reduce conflict",
    "LR grammar",
    "LR(1) grammar",
    "LR(k) grammar",
    "LR(k)"
  ],
  "Parser combinator": [
    "Parser combinators",
    "Combinatory parsing",
    "Parser Combinator",
    "Combinator parser"
  ]
}
```
