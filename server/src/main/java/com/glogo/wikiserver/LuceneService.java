package com.glogo.wikiserver;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.filechooser.FileSystemView;

import net.lingala.zip4j.core.ZipFile;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Singleton wrapper service class for reading data from CSV resource
 * and searching documents in Lucene.
 * For detailed information please see <a href="https://github.com/Glogo/wikipedia-redirects">project repository</a>
 * @author Glogo
 */
public class LuceneService {
	
	private static Logger logger = LoggerFactory.getLogger(LuceneService.class);
	
	/**
	 * Name of resource containing zipped data
	 */
	private static final String dataResourceZipFile = "data.csv.zip";
	
	/**
	 * Name of CSV file in zipped file containing info data
	 */
	private static final String dataInfoFile = "data_info.csv";
	
	/**
	 * Name of CSV file in zipped file containing alternative titles data
	 */
	private static final String dataAlternativeTitlesFile = "data.csv";
	
	/**
	 * Directory where will be data unzipped (default user home directory/wiki-redirects-data)
	 */
	private static final String unzipDirectory = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + "/wiki-redirects-data";

	private static LuceneService instance = null;
	private StandardAnalyzer luceneAnalyzer = new StandardAnalyzer();
	private Directory luceneIndex = new RAMDirectory();
	private IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LATEST, luceneAnalyzer);
	private Map<String, Object> dataInfoMap = new HashMap<String, Object>();
	
	/**
	 * Map of pages ids to array of alternative titles
	 */
	private Map<Integer, String[]> allAlternativeTitlesMap = new HashMap<Integer, String[]>();
	
	/**
	 * Initializes lucene, reads input json file & adds information about redirects to lucene
	 */
	private LuceneService() {
		initUnzipDir();
		unzipData();
		readDataInfo();
		readAlternativeTitlesData();
	}
	
	/**
	 * Create [& delete] directory for unzip
	 */
	private void initUnzipDir() {
		File unzipDirectoryFile = new File(unzipDirectory);
		if(unzipDirectoryFile.exists()){
			logger.info("Deleting unzip directory: '{}'", unzipDirectory);
			unzipDirectoryFile.delete();
		}
		try{
			logger.info("Creating unzip directory: '{}'", unzipDirectory);
			unzipDirectoryFile.mkdir();
		}catch(Exception e){
			logger.error("Unzip directory could not be created.");
			logger.error(e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Unzip data to unzip dir
	 */
	private void unzipData() {
		try{
			// Get zipped data resource file url
			URL url = getClass().getClassLoader().getResource(dataResourceZipFile);
			
			// Decode url to remove %20 space sepparators
			String source = URLDecoder.decode(url.getFile(), "UTF-8");
			
			// Do unzip
			logger.info("Unzipping data archive from: '{}' to: '{}'", source, unzipDirectory);
			ZipFile zipFile = new ZipFile(source);
			zipFile.extractAll(unzipDirectory);
			
		}catch(Exception e)	{
			logger.error(e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Reads info data to simple map
	 */
	private void readDataInfo() {
		try {
			/*
			 * Read info data to simple map
			 */
			logger.info("Reading data info");
			CSVReader reader = new CSVReader(new FileReader(unzipDirectory + "/" + dataInfoFile));
		    String[] headerCols = reader.readNext();
		    String[] infoCols = reader.readNext();
		    reader.close();
		    
		    dataInfoMap.clear();
		    
		    // Add all columns & data to map
		    dataInfoMap.put(headerCols[0], infoCols[0]);
		    dataInfoMap.put(headerCols[1], Integer.parseInt(infoCols[1]));
		    dataInfoMap.put(headerCols[2], Integer.parseInt(infoCols[2]));
		    dataInfoMap.put(headerCols[3], Integer.parseInt(infoCols[3]));
		    dataInfoMap.put(headerCols[4], Integer.parseInt(infoCols[4]));

		} catch (IOException e) {
			logger.error(e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Reads alternative titles to lucene index
	 */
	private void readAlternativeTitlesData() {
		try {
		    /*
		     * Read alternative titles data to Lucene
		     */
		    logger.info("Reading & indexing alternative titles data");
		    IndexWriter writer = new IndexWriter(luceneIndex, indexWriterConfig);
		    CSVReader reader = new CSVReader(new FileReader(unzipDirectory + "/" + dataAlternativeTitlesFile));
		    String[] row;
		    
		    allAlternativeTitlesMap.clear();
		    
		    // Iterate over each row from CSV file
		    int index = 0;
		    while ((row = reader.readNext()) != null) {
		    	addPage(writer, index++, row[0], Arrays.copyOfRange(row, 1, row.length));
		    }
		    reader.close();
		    writer.close();
		    logger.info("Reading & indexing done");

		} catch (IOException e) {
			logger.error(e.getMessage());
			System.exit(1);
		}
	}
	
	/**
	 * Create page document and index it in Lucene
	 * @param writer
	 * @param index
	 * @param title
	 * @param alternativeTitles
	 * @throws IOException
	 */
	private void addPage(IndexWriter writer, int index, String title, String[] alternativeTitles) throws IOException {
		Document doc = new Document();
		doc.add(new IntField("id", index, Field.Store.YES));
		doc.add(new TextField("title", title, Field.Store.YES));
		writer.addDocument(doc);
		
		// Put alternative titles to map to enable easy access when querying
		allAlternativeTitlesMap.put(index, alternativeTitles);
	}
	
	/**
	 * Searches Lucene index and returns Map of found pages mapped to array of alternative titles
	 * @param query
	 * @return
	 */
	public Map<String, String[]> searchPages(String query) {
		Map<String, String[]> output = new LinkedHashMap<String, String[]>();
		
		try {
			Query luceneQuery = new QueryParser("title", luceneAnalyzer).parse(query);
			
			int hitsPerPage = 20;
			DirectoryReader reader = DirectoryReader.open(luceneIndex);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
			searcher.search(luceneQuery, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			
			logger.info("Found " + hits.length + " hits.");
			for(int i=0;i<hits.length;++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				output.put(d.get("title"), allAlternativeTitlesMap.get(d.getField("id").numericValue().intValue()));
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	public Map<String, Object> getDataInfoMap() {
		return dataInfoMap;
	}
	
	synchronized public static LuceneService getInstance() {
		if(instance == null){
			instance = new LuceneService();
		}
		
		return instance;
	}
}
