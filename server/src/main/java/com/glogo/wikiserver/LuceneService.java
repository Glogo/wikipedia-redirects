package com.glogo.wikiserver;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.filechooser.FileSystemView;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Singleton wrapper service class for adding and searching documents in lucene
 * @author Glogo
 */
public class LuceneService {
	
	// Singleton instance
	private static LuceneService instance = null;
	
	// Lucene
	private StandardAnalyzer analyzer = new StandardAnalyzer();
	private Directory index = new RAMDirectory();
	private IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
	private IndexWriter w;
	
	// Data
	Map<String, List<String>> pages;
	
	/**
	 * Initializes lucene, reads input json file & adds information about redirects to lucene
	 */
	private LuceneService() {
		try {
			// Open json data archive
			URL url = getClass().getClassLoader().getResource("data.min.json.zip");
			
			// Extract it somewhere on system
			String source = url.getFile();
		    String destination = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath(); // default user directory
		    System.out.println(destination);
		    
		    // Read destination file
		    byte[] encoded = Files.readAllBytes(Paths.get(destination+"/data.min.json"));
		    String destinationContent = new String(encoded, Charset.forName("UTF-8"));
		    
		    System.out.println("extracting");
		    
		    try {
		         ZipFile zipFile = new ZipFile(source);
		         zipFile.extractAll(destination);
		    } catch (ZipException e) {
		        e.printStackTrace();
		    }
			
			Gson gson = new GsonBuilder().create();
			System.out.println("reading");
			Map json = gson.fromJson(destinationContent, Map.class);
			pages = (Map) json.get("pages");
			
			w = new IndexWriter(index, config);
			
			// Iterate over pages
			System.out.println("indexing");
			for(Entry<String, List<String>> entry : pages.entrySet()) {
				//System.out.println(entry.getKey());
				addDoc(w, entry.getKey(), null); // TODO alt + remember
			}
			
			System.out.println("init done");
			
			w.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void addDoc(IndexWriter w, String title, String isbn) throws IOException {
		  Document doc = new Document();
		  doc.add(new TextField("title", title, Field.Store.YES));
		  //doc.add(new StringField("isbn", isbn, Field.Store.YES));
		  w.addDocument(doc);
		}
	//http://localhost:8080/WikipediaRedirectsServer/webapi/getRedirects/Information
	public Map<String, List<String>> searchPages(String query) {
		Map<String, List<String>> output = new LinkedHashMap<String, List<String>>();
		
		try {
			Query q = new QueryParser("title", analyzer).parse(query);
			
			int hitsPerPage = 20;
			DirectoryReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
			searcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			
			
			
			System.out.println("Found " + hits.length + " hits.");
			for(int i=0;i<hits.length;++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				output.put(d.get("title"), pages.get(d.get("title")));
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	public static LuceneService getInstance() {
		if(instance == null){
			instance = new LuceneService();
		}
		
		return instance;
	}
}
