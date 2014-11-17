# Wikipedia Alternative Titles
Main goal of this project was to implement parser for finding alternative titles for Wikipedia pages by parsing [articles XML dump files](https://dumps.wikimedia.org/enwiki/). Amongst other detailed information, in each page record we can find page title and flag if this page is redirect to another page. If this page is redirect we can consider its title as alternative title of page it is referring to.

Please note that this project does not bring any new exciting functionality. Wikipedia provides online services such as ["What links here"](http://en.wikipedia.org/w/index.php?title=Special%3AWhatLinksHere&target=Computer&namespace=) where you can find amongst other things pages referring to specified page. This project was mostly a challenge because input XML files are larger than 50 GB of more than 14 mil pages records.

**This repository contains two Java projects:**

1. [Parser](parser) - parsing Wikipedia XML dumps and saving alternative titles data to CSV file
2. [Server](server) - read alternative titles from file, index them in Lucene, provide REST services for page search and webpage to display results
