#Parser
Java project responsible for parsing Wikipedia XML dumps and saving alternative titles data to CSV file

## Eclipse project setup
1. Import project as Java or Maven Project
2. Open Run Configurations and add following two program arguments: `<Wikipedia dump XML>` `<Output file>`. Make sure you enter absolute paths
3. To VM Arguments add `-Xmx6g`
4. Click apply and run

**Simplified example of Wikipedia XML dump:**
```xml
<mediawiki version="0.9" xml:lang="en">
	<page>
		<title>Hound</title>
		<redirect title="Dog" />
	</page>
	<page>
		<title>Bark Machine</title>
		<redirect title="Dog" />
	</page>
	<page>
		<title>Puppy</title>
		<redirect title="Dog" />
	</page>
	<page>
		<title>Dog</title>
	</page>
	<page>
		<title>Football</title>
	</page>
	<page>
		<title>Soccer</title>
		<redirect title="Football" />
	</page>
</mediawiki>
```

**Alternative titles CSV output:**
```csv
"Dog","Hound","Bark Machine","Puppy"
"Football","Soccer"
```

Where first columns always contains title of page and other columns are titles of pages redirected to this page. So page with title `"Dog"` has alternative titles `"Hound"`,`"Bark Machine"` and `"Puppy"`
